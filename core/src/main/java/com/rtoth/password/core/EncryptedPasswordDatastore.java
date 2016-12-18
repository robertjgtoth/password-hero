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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Encapsulates a datastore for passwords in {@link java.util.Properties} format.
 */
public interface EncryptedPasswordDatastore
{
    /**
     * Get an input stream from which applications and their corresponding encrypted passwords can be read in
     * {@link java.util.Properties} format.
     *
     * @return An input stream from which applications and their corresponding encrypted passwords can be read in
     *         {@link java.util.Properties} format. Never {@code null}.
     *
     * @throws IOException If there is a problem obtaining the input stream.
     */
    InputStream getInputStream() throws IOException;

    /**
     * Get an output stream to which applications and their corresponding encrypted passwords can be stored in
     * {@link java.util.Properties} format.
     *
     * @return An output stream to which applications and their corresponding encrypted passwords can be stored in
     *         {@link java.util.Properties} format. Never {@code null}.
     *
     * @throws IOException If there is a problem obtaining the output stream.
     */
    OutputStream getOutputStream() throws IOException;
}
