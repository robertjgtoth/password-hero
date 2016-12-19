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
package com.rtoth.password.android;

import com.google.common.base.Preconditions;
import com.rtoth.password.core.PasswordManager;

/**
 * FIXME: docs
 */
public final class UserEnvironment
{
    private static final UserEnvironment INSTANCE = new UserEnvironment();

    private PasswordManager passwordManager;

    private UserEnvironment()
    {
        // Private constructor for singleton
    }

    public static UserEnvironment getInstance()
    {
        return INSTANCE;
    }

    public PasswordManager getPasswordManager()
    {
        if (passwordManager == null)
        {
            throw new IllegalStateException("Illegal attempt to access passwordManager before initialization.");
        }
        return passwordManager;
    }

    public void setPasswordManager(PasswordManager passwordManager)
    {
        if (this.passwordManager != null)
        {
            throw new IllegalStateException("Illegal attempt to set passwordManager when its already been set.");
        }
        this.passwordManager = Preconditions.checkNotNull(passwordManager, "passwordManager cannot be null.");
    }
}
