/*
 * Copyright (c) 2016 Robert Toth
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.rtoth.password.data;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * FIXME: docs
 */
public class PasswordManager
{
    /** Logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordManager.class);

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // FIXME: Make this range configurable or something.
    private final PasswordGenerator passwordGenerator = new AsciiPasswordGenerator(20, 30);

    private final ObservableList<String> availableApplications = FXCollections.observableArrayList();

    private final Map<String, String> passwordsByApplication = Maps.newHashMap();

    private final ReadWriteLock passwordsLock = new ReentrantReadWriteLock();

    private StandardPBEStringEncryptor encryptor;

    /** File used to store encrypted passwords. */
    private final File passwordFile;

    public PasswordManager(String filePath, String masterPassword)
    {
        Preconditions.checkNotNull(filePath, "passwordFile cannot be null.");
        Preconditions.checkNotNull(masterPassword, "masterPassword cannot be null.");

        passwordFile = new File(filePath);

        if (!passwordFile.exists() || passwordFile.isDirectory() ||
            !passwordFile.canRead() || !passwordFile.canWrite())
        {
            String message = "Password file is not a regular file with rw permissions: " + filePath;
            LOGGER.warn(message);
            throw new IllegalArgumentException(message);
        }

        encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(masterPassword);

        try
        {
            loadExistingPasswords();
        }
        catch (IOException e)
        {
            LOGGER.error("Unable to load existing passwords from file.", e);
            throw new IllegalStateException(e);
        }
    }

    private void loadExistingPasswords() throws IOException
    {
        Properties encryptedFileContents = new Properties();
        FileInputStream fis = new FileInputStream(passwordFile);
        encryptedFileContents.load(fis);
        fis.close();
        for (Map.Entry<Object, Object> entry : encryptedFileContents.entrySet())
        {
            String application = encryptor.decrypt((String) entry.getKey());
            passwordsByApplication.put(
                application,
                encryptor.decrypt((String) entry.getValue())
            );
            availableApplications.add(application);
        }
    }

    public ObservableList<String> getAvailableApplications()
    {
        return availableApplications;
    }

    public boolean hasPassword(String applicationName)
    {
        Preconditions.checkNotNull(applicationName, "applicationName cannot be null.");

        passwordsLock.readLock().lock();
        try
        {
            return passwordsByApplication.containsKey(applicationName);
        }
        finally
        {
            passwordsLock.readLock().unlock();
        }
    }

    public String getPassword(String applicationName)
        throws EncryptionOperationNotPossibleException
    {
        Preconditions.checkNotNull(applicationName, "applicationName cannot be null.");

        passwordsLock.readLock().lock();
        try
        {
            return passwordsByApplication.get(applicationName);
        }
        finally
        {
            passwordsLock.readLock().unlock();
        }
    }

    public void generatePassword(String applicationName)
        throws EncryptionOperationNotPossibleException
    {
        Preconditions.checkNotNull(applicationName, "applicationName cannot be null.");

        passwordsLock.writeLock().lock();
        try
        {
            passwordsByApplication.put(applicationName, passwordGenerator.generatePassword());
            // Only add it to the list if it's not already in there
            if (!availableApplications.contains(applicationName))
            {
                availableApplications.add(applicationName);
            }
            executorService.submit(new StorePasswordTask());
        }
        finally
        {
            passwordsLock.writeLock().unlock();
        }
    }

    public void deletePassword(String applicationName)
    {
        Preconditions.checkNotNull(applicationName, "applicationName cannot be null.");

        passwordsLock.writeLock().lock();
        try
        {

            passwordsByApplication.remove(applicationName);
            availableApplications.remove(applicationName);
            executorService.submit(new StorePasswordTask());
        }
        finally
        {
            passwordsLock.writeLock().unlock();
        }
    }

    public void changePassword(String applicationName)
    {
        generatePassword(applicationName);
    }

    public void changeMasterPassword(String newMasterPassword)
    {
        Preconditions.checkNotNull(newMasterPassword, "newMasterPassword cannot be null.");

        passwordsLock.readLock().lock();
        try
        {
            // Cannot set the password of an encryptor after it's been created,
            // so need to create a new one here instead.
            encryptor = new StandardPBEStringEncryptor();
            encryptor.setPassword(newMasterPassword);
            executorService.submit(new StorePasswordTask());
        }
        finally
        {
            passwordsLock.readLock().unlock();
        }
    }

    private final class StorePasswordTask implements Runnable
    {
        @Override
        public void run()
        {
            passwordsLock.readLock().lock();
            try
            {
                Properties encryptedFileContents = new Properties();
                for (Map.Entry<String, String> entry : passwordsByApplication.entrySet())
                {
                    encryptedFileContents.setProperty(
                        encryptor.encrypt(entry.getKey()),
                        encryptor.encrypt(entry.getValue())
                    );
                }

                FileOutputStream fos = new FileOutputStream(passwordFile);
                encryptedFileContents.store(fos, "PasswordsByFile");
                fos.flush();
                fos.close();
                LOGGER.info("Encrypted passwords saved to file.");
            }
            catch (Exception e)
            {
                // FIXME this...
                LOGGER.warn("Error storing password in file...", e);
            }
            finally
            {
                passwordsLock.readLock().unlock();
            }
        }
    }
}
