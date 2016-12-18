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
 * Manages CRUD operations on application passwords, including
 * storage/retrieval of encrypted data from the filesystem.
 */
public class PasswordManager
{
    /** Logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordManager.class);

    /** Used to do file IO in the background. */
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /** Generates new random passwords. */
    // FIXME: Make this range configurable or something.
    private final RandomPasswordGenerator passwordGenerator = new AsciiPasswordGenerator(20, 30);

    /** Observable list containing all applications with a password stored. */
    private final ObservableList<String> availableApplications = FXCollections.observableArrayList();

    /** Map of plaintext passwords by application. */
    // FIXME: Figure out a way to make this not plaintext.
    private final Map<String, String> passwordsByApplication = Maps.newHashMap();

    /** Locks access to {@code #passwordsByApplication}. */
    private final ReadWriteLock passwordsLock = new ReentrantReadWriteLock();

    /** File used to store encrypted passwords. */
    private final File passwordFile;

    /** Used to perform encryption and decryption. */
    private StandardPBEStringEncryptor encryptor;

    /**
     * Create a new {@link PasswordManager} using the provided file path and master password.
     *
     * @param filePath File path where encrypted passwords are stored. Cannot be {@code null}.
     * @param masterPassword Plaintext master password to use. This should be the password previously used to
     *                       encrypt the passwords stored in {@code filePath}, or a new master password if there
     *                       are no passwords stored yet. Cannot be {@code null}.
     *
     * @throws EncryptionOperationNotPossibleException if there are existing passwords in the file, and the provided
     *         {@code masterPassword} is not correct.
     * @throws IOException if there is some IO issue reading the provided {@code filePath}.
     * @throws NullPointerException if {@code filePath} or {@code masterPassword} is {@code null}.
     */
    public PasswordManager(String filePath, String masterPassword)
        throws EncryptionOperationNotPossibleException, IOException
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

        loadExistingPasswords();
    }

    /**
     * Load any existing encrypted passwords from the {@code passwordsFile} into memory.
     *
     * @throws EncryptionOperationNotPossibleException if there are existing passwords in the file, and the
     *         {@code masterPassword} used to initialize the encryptor is not correct.
     * @throws IOException if there is some IO issue reading the {@code passwordsFile}.
     */
    private void loadExistingPasswords() throws EncryptionOperationNotPossibleException, IOException
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

    /**
     * Get an observable list of all applications with passwords managed by this application.
     * <p>
     * As new passwords are generated for applications, or applications have their passwords deleted, this list will be
     * updated by this class for the benefit of anyone listening.
     *
     * @return An observable list of all applications with passwords managed by this application. Never {@code null},
     *         but may be empty.
     */
    public ObservableList<String> getAvailableApplications()
    {
        return availableApplications;
    }

    /**
     * Get whether we have a stored password for the provided application.
     *
     * @param applicationName Application to check. Cannot be {@code null}.
     * @return {@code true} if we have a password stored for the application, {@code false} otherwise.
     *
     * @throws NullPointerException if {@code applicationName} is {@code null}.
     */
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

    /**
     * Get the plaintext password for the provided application.
     *
     * @param applicationName Application for which to get the password. Cannot be {@code null}.
     * @return The plaintext password for the provided application, or {@code null} if there is no password stored.
     *
     * @throws NullPointerException if {@code applicationName} is {@code null}.
     */
    public String getPlaintextPassword(String applicationName)
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

    /**
     * Generate a new, random password for the provided application.
     * <p>
     * If a password is already stored for the provided application, a new one will be created and will overwrite
     * the existing one.
     *
     * @param applicationName Application for which to generate and save a new password. Cannot be {@code null}.
     *
     * @throws NullPointerException if {@code applicationName} is {@code null}.
     */
    public void generatePassword(String applicationName)
    {
        Preconditions.checkNotNull(applicationName, "applicationName cannot be null.");

        passwordsLock.writeLock().lock();
        try
        {
            passwordsByApplication.put(applicationName, passwordGenerator.generatePassword());
            availableApplications.add(applicationName);
            executorService.submit(new StorePasswordTask());
        }
        finally
        {
            passwordsLock.writeLock().unlock();
        }
    }

    /**
     * Delete the password associated with the provided application.
     *
     * @param applicationName Application for which to delete the password. Cannot be {@code null}.
     *
     * @throws NullPointerException if {@code applicationName} is {@code null}.
     */
    public void deletePassword(String applicationName)
    {
        Preconditions.checkNotNull(applicationName, "applicationName cannot be null.");

        passwordsLock.writeLock().lock();
        try
        {
            if (hasPassword(applicationName))
            {
                passwordsByApplication.remove(applicationName);
                availableApplications.remove(applicationName);
                executorService.submit(new StorePasswordTask());
            }
        }
        finally
        {
            passwordsLock.writeLock().unlock();
        }
    }

    /**
     * Change the provided application's password by generating a new, random password.
     *
     * @param applicationName Application for which to generate and save a new password. Cannot be {@code null} and
     *                        must have an existing password.
     *
     * @throws IllegalArgumentException if {@code applicationName} does not have an existing password.
     * @throws NullPointerException if {@code applicationName} is {@code null}.
     */
    // FIXME: Threading bug likely here if someone invokes delete before we grab the lock
    public void changePassword(String applicationName)
    {
        Preconditions.checkNotNull(applicationName, "applicationName cannot be null.");
        Preconditions.checkArgument(hasPassword(applicationName),
            "Cannot change password for unknown application: " + applicationName);

        passwordsLock.writeLock().lock();
        try
        {
            String existingPassword = passwordsByApplication.get(applicationName);
            String newPassword;
            do
            {
                newPassword = passwordGenerator.generatePassword();
            }
            while (newPassword.equals(existingPassword));

            passwordsByApplication.put(applicationName, newPassword);
            executorService.submit(new StorePasswordTask());
        }
        finally
        {
            passwordsLock.writeLock().unlock();
        }
    }

    /**
     * Change the master password to the provided value.
     *
     * @param newMasterPassword New plaintext master password to use. Cannot be {@code null}.
     *
     * @throws NullPointerException if {@code newMasterPassword} is {@code null}.
     */
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

    /**
     * Runnable task to encrypt and store the current password map to disk.
     */
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
