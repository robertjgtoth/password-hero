/*
 * Inspiration: http://www.jensd.de/wordpress/?p=132
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

import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Utility class used to create buttons and labels using FontAwesome icons.
 */
public final class FontAwesomeUtility
{
    /**
     * Private constructor for utility class.
     */
    private FontAwesomeUtility()
    {
        // Nothing to see here.
    }

    // FIXME: Rest of documentation here.

    public static Button createIconButton(FontAwesomeIcon icon)
    {
        return createIconButton(icon, "", 16);
    }

    public static Button createIconButton(FontAwesomeIcon icon, String text)
    {
        return createIconButton(icon, text, 16);
    }

    public static Button createIconButton(FontAwesomeIcon icon, int iconSize)
    {
        return createIconButton(icon, "", iconSize);
    }

    public static Button createIconButton(FontAwesomeIcon icon, String text, int iconSize)
    {
        Label iconLabel = createIconLabel(icon);
        iconLabel.setStyle("-fx-font-size: " + iconSize + "px;");

        Button button = new Button();
        button.setText(text);
        button.setGraphic(iconLabel);

        return button;
    }

    public static Label createIconLabel(FontAwesomeIcon icon, String style)
    {
        Label label = new Label();
        label.setText(icon.getUnicodeString());
        label.setStyle(style);

        return label;
    }

    public static Label createIconLabel(FontAwesomeIcon icon)
    {
        return createIconLabel(icon, 16);
    }

    public static Label createIconLabel(FontAwesomeIcon icon, int iconSize)
    {
        Label label = new Label();
        label.setText(icon.getUnicodeString());
        label.getStyleClass().clear();
        label.getStyleClass().add("icons");
        label.setStyle("-fx-font-size: " + iconSize + "px;");

        return label;
    }
}
