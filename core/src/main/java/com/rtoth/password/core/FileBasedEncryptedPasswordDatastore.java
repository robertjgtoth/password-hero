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
package com.rtoth.password.core;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * {@link EncryptedPasswordDatastore} that uses a flat file.
 */
public class FileBasedEncryptedPasswordDatastore implements EncryptedPasswordDatastore
{
    /** File used to store data. */
    private final File passwordFile;

    /**
     * Create a new {@link FileBasedEncryptedPasswordDatastore} using the provided {@link File}.
     *
     * @param passwordFile {@link File} in which encrypted passwords will be stored. Cannot be {@code null}, and must
     *                     be an existing regular file with read and write permissions.
     *
     * @throws IllegalArgumentException if {@code passwordFile} is not an existing regular file with read and write
     *         permissions.
     * @throws NullPointerException if {@code passwordFile} is {@code null}.
     */
    public FileBasedEncryptedPasswordDatastore(File passwordFile)
    {
        this.passwordFile = Preconditions.checkNotNull(passwordFile, "passwordFile cannot be null.");
        Preconditions.checkArgument(
            passwordFile.exists() && !passwordFile.isDirectory() &&
                passwordFile.canRead() && passwordFile.canWrite(),
            "passwordFile must be a regular file with rw permissions.");
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
        return new FileInputStream(passwordFile);
    }

    @Override
    public OutputStream getOutputStream() throws IOException
    {
        return new FileOutputStream(passwordFile);
    }
}
