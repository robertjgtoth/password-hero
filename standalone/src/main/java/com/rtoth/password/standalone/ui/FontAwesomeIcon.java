/*
 * Inspiration: Source: http://www.jensd.de/wordpress/?p=132
 *
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
package com.rtoth.password.standalone.ui;

import com.google.common.base.Preconditions;

/**
 * Enumeration of FontAwesome icons.
 */
public enum FontAwesomeIcon
{
    /** Refresh icon. */
    REFRESH("\uf021"),

    /** Open eyeball icon. */
    OPEN_EYE("\uf06e"),

    /** Trash can icon. */
    TRASH("\uf1f8");

    /** Unicode representation of this icon. */
    private final String unicodeString;

    /**
     * Create a new {@link FontAwesomeIcon} using the provided unicode string.
     *
     * @param unicodeString Unicode representation of the icon. Cannot be {@code null}.
     *
     * @throws NullPointerException if {@code unicodeString} is {@code null}.
     */
    FontAwesomeIcon(String unicodeString)
    {
        this.unicodeString = Preconditions.checkNotNull(unicodeString, "unicodeString cannot be null.");
    }

    /**
     * Get the unicode representation of this icon.
     *
     * @return Unicode representation of the icon. Never {@code null}.
     */
    public String getUnicodeString()
    {
        return unicodeString;
    }
}
