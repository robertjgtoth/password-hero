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
package com.rtoth.password.ui;

import com.rtoth.password.data.PasswordManager;

import org.jasypt.exceptions.EncryptionOperationNotPossibleException;

import java.util.Optional;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * FIXME: docs
 */
public class ApplicationController extends Application
{
    private final PasswordManager passwordManager =
        new PasswordManager("C:\\Users\\rtoth\\AppData\\Local\\PasswordHero\\encrypted.gpg");

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        AnchorPane root = new AnchorPane();
        root.setPrefHeight(600.0);
        root.setPrefWidth(400.0);

        final ListView<String> applicationPasswords =
            new ListView<>(passwordManager.getAvailableApplications());
        applicationPasswords.setCellFactory(param -> new ApplicationPasswordCell());

        AnchorPane.setTopAnchor(applicationPasswords, 0.0);
        AnchorPane.setLeftAnchor(applicationPasswords, 0.0);
        AnchorPane.setRightAnchor(applicationPasswords, 0.0);
        root.getChildren().add(applicationPasswords);

        Button addNewApplication = new Button("Add new application");
        addNewApplication.setOnAction(event ->
        {
            TextInputDialog textInputDialog = new TextInputDialog();
            textInputDialog.setTitle("New application");
            textInputDialog.setHeaderText("Add new application");
            textInputDialog.setContentText("Enter application name");

            Optional<String> newApplication = textInputDialog.showAndWait();
            if (newApplication.isPresent())
            {
                if (!passwordManager.hasPassword(newApplication.get()))
                {
                    Optional<String> masterPassword = getMasterPassword();
                    if (masterPassword.isPresent())
                    {
                        passwordManager.generatePassword(newApplication.get(), masterPassword.get());
                        showApplicationPassword(newApplication.get(), masterPassword);
                    }
                }
                else
                {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Application already registered!", ButtonType.OK);
                    alert.showAndWait();
                }
            }
        });

        AnchorPane.setLeftAnchor(addNewApplication, 0.0);
        AnchorPane.setRightAnchor(addNewApplication, 0.0);
        AnchorPane.setBottomAnchor(addNewApplication, 0.0);
        root.getChildren().add(addNewApplication);

        primaryStage.setTitle("Password Hero");
        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.show();
    }

    private Optional<String> getMasterPassword()
    {
        PasswordDialog passwordDialog = new PasswordDialog();
        passwordDialog.setHeaderText("Enter master password");
        return passwordDialog.showAndWait();
    }

    private void showApplicationPassword(String application, Optional<String> masterPassword)
    {
        if (masterPassword.isPresent())
        {
            try
            {
                String savedPassword = passwordManager.getPassword(application, masterPassword.get());
                if (savedPassword != null)
                {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, savedPassword, ButtonType.OK);
                    alert.setTitle("Saved password");
                    alert.setHeaderText("Saved password for " + application + "");

                    TextField savedPasswordText = new TextField(savedPassword);
                    savedPasswordText.setEditable(false);
                    HBox.setHgrow(savedPasswordText, Priority.ALWAYS);

                    HBox hBox = new HBox();
                    hBox.getChildren().add(savedPasswordText);
                    hBox.setPadding(new Insets(20));

                    alert.getDialogPane().setContent(hBox);

                    alert.showAndWait();
                }
            }
            catch (EncryptionOperationNotPossibleException e)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid password!", ButtonType.OK);
                alert.showAndWait();
            }
        }
        else
        {
            // What??
        }
    }

    private void deleteApplicationPassword(String application)
    {
        // FIXME: Make delete work
        passwordManager.deletePassword(application);
    }

    private final class ApplicationPasswordCell extends ListCell<String>
    {
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null)
            {
                HBox hBox = new HBox(5);
                Label applicationName = new Label(item);
                Button show = new Button("show");
                show.setOnAction(event -> showApplicationPassword(item, getMasterPassword()));
                Button delete = new Button("delete");
                delete.setOnAction(event -> deleteApplicationPassword(item));

                hBox.getChildren().addAll(applicationName, show, delete);
                setGraphic(hBox);
            }
        }
    }

    /**
     * Main method for standalone testing.
     *
     * @param args Arguments to use.
     */
    public static void main(String args[])
    {
        launch(args);
    }
}
