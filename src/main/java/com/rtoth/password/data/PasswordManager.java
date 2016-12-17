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

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    private final Properties passwordsByApplication = new Properties();

    /** File used to store encrypted passwords. */
    private final File passwordFile;

    public PasswordManager(String filePath)
    {
        Preconditions.checkNotNull(filePath, "passwordFile cannot be null.");

        passwordFile = new File(filePath);

        if (!passwordFile.exists() || passwordFile.isDirectory() ||
            !passwordFile.canRead() || !passwordFile.canWrite())
        {
            String message = "Password file is not a regular file with rw permissions: " + filePath;
            LOGGER.warn(message);
            throw new IllegalArgumentException(message);
        }

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
        passwordsByApplication.load(new FileInputStream(passwordFile));
        for (Object key : passwordsByApplication.keySet())
        {
            availableApplications.add((String) key);
        }
    }

    public ObservableList<String> getAvailableApplications()
    {
        return availableApplications;
    }

    public boolean hasPassword(String applicationName)
    {
        Preconditions.checkNotNull(applicationName, "applicationName cannot be null.");

        return passwordsByApplication.containsKey(applicationName);
    }

    public String getPassword(String applicationName, String masterPassword)
        throws EncryptionOperationNotPossibleException
    {
        Preconditions.checkNotNull(applicationName, "applicationName cannot be null.");
        Preconditions.checkNotNull(masterPassword, "masterPassword cannot be null.");

        StandardPBEStringEncryptor passwordEncryptor = new StandardPBEStringEncryptor();
        passwordEncryptor.setPassword(masterPassword);

        return passwordEncryptor.decrypt(passwordsByApplication.getProperty(applicationName));
    }

    public void generatePassword(String applicationName, String masterPassword)
        throws EncryptionOperationNotPossibleException
    {
        Preconditions.checkNotNull(applicationName, "applicationName cannot be null.");
        Preconditions.checkNotNull(masterPassword, "masterPassword cannot be null.");

        StandardPBEStringEncryptor passwordEncryptor = new StandardPBEStringEncryptor();
        passwordEncryptor.setPassword(masterPassword);

        passwordsByApplication.setProperty(applicationName,
            passwordEncryptor.encrypt(passwordGenerator.generatePassword()));
        availableApplications.add(applicationName);

        executorService.submit(new StorePasswordTask());
    }

    public void deletePassword(String applicationName)
    {
        Preconditions.checkNotNull(applicationName, "applicationName cannot be null.");

        passwordsByApplication.remove(applicationName);
        availableApplications.remove(applicationName);

        executorService.submit(new StorePasswordTask());
    }

    private final class StorePasswordTask implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
                passwordsByApplication.store(new FileOutputStream(passwordFile), "PasswordsByFile");
                LOGGER.info("Password saved to file.");
            }
            catch (Exception e)
            {
                // FIXME this...
                LOGGER.warn("Error storing password in file...", e);
            }

        }
    }

    /** Main method for testing purposes. */
    public static void main(String args[])
    {
        if (args.length == 1)
        {
            try
            {
                String filePath = args[0];

                Scanner s = new Scanner(System.in);

                PasswordManager passwordManager = new PasswordManager(filePath);

                while (true)
                {

                    System.out.print("Enter application name: ");
                    String applicationName = s.nextLine();

                    System.out.print("Enter master password: ");
                    String masterPassword = s.nextLine();

                    LOGGER.info("You entered {}", applicationName);

                    String password;
                    if (!passwordManager.hasPassword(applicationName))
                    {
                        passwordManager.generatePassword(applicationName, masterPassword);
                    }
                    password = passwordManager.getPassword(applicationName, masterPassword);

                    LOGGER.info("Your stored password for {} is {}", applicationName, password);
                }
            }
            catch (RuntimeException re)
            {
                LOGGER.warn("Bad file", re);
                System.exit(-1);
            }
        }
        else
        {
            LOGGER.warn("Please provide the password file location.");
        }
    }
}
