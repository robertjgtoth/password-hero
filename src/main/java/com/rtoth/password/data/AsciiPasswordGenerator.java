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

import java.security.SecureRandom;

/**
 * FIXME: docs
 */
public class AsciiPasswordGenerator implements PasswordGenerator
{
    // !
    private static final int ASCII_MIN = 33;

    // ~
    private static final int ASCII_MAX = 126;

    private static final int ABSOLUTE_MIN_CHARS = 15;

    private static final int ABSOLUTE_MAX_CHARS = 100;

    private final SecureRandom random = new SecureRandom();

    private final int minCharacters;

    private final int maxCharacters;

    public AsciiPasswordGenerator(int minCharacters, int maxCharacters)
    {
        Preconditions.checkArgument(minCharacters < maxCharacters,
            "minCharacters must be < maxCharacters");
        Preconditions.checkArgument(minCharacters >= ABSOLUTE_MIN_CHARS,
            "minCharacters must be >= " + ABSOLUTE_MIN_CHARS);
        Preconditions.checkArgument(maxCharacters <= ABSOLUTE_MAX_CHARS,
            "maxCharacters must be <= " + ABSOLUTE_MAX_CHARS);

        this.minCharacters = minCharacters;
        this.maxCharacters = maxCharacters;
    }

    @Override
    public String generatePassword()
    {
        int length = randomInRange(minCharacters, maxCharacters);
        StringBuilder passwordBuilder = new StringBuilder();
        for (int i = 0; i < length; i++)
        {
            passwordBuilder.append(randomAsciiCharacter());
        }

        return passwordBuilder.toString();
    }

    private char randomAsciiCharacter()
    {
        return (char) randomInRange(ASCII_MIN, ASCII_MAX);
    }

    private int randomInRange(int minInclusive, int maxInclusive)
    {
        return random.nextInt((maxInclusive - minInclusive) + 1) + minInclusive;
    }
}
