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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

/**
 * FIXME: docs
 */
public class ApplicationController extends Application
{
    private static final double MIN_APPLICATION_WIDTH = 300.0;

    private static final double MIN_APPLICATION_HEIGHT = 250.0;

    // FIXME: Make this configurable
    private static final String FILE_LOCATION = "C:\\Users\\rtoth\\AppData\\Local\\PasswordHero\\encrypted.gpg";

    private PasswordManager passwordManager;

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        Optional<String> masterPassword;
        do
        {
            masterPassword = getMasterPassword();
        }
        while (!masterPassword.isPresent());

        try
        {
            passwordManager = new PasswordManager(FILE_LOCATION, masterPassword.get());

            AnchorPane root = new AnchorPane();
            root.setPrefWidth(MIN_APPLICATION_WIDTH);
            root.setMinWidth(MIN_APPLICATION_WIDTH);
            root.setPrefHeight(MIN_APPLICATION_HEIGHT);
            root.setMinHeight(MIN_APPLICATION_HEIGHT);

            // FIXME: This ListView is sometimes not rendering properly when items
            //        are deleted. Gotta figure out what's going on there.
            final ListView<String> applicationPasswords =
                new ListView<>(passwordManager.getAvailableApplications());
            applicationPasswords.setCellFactory(param -> new ApplicationPasswordCell());

            AnchorPane.setTopAnchor(applicationPasswords, 0.0);
            AnchorPane.setLeftAnchor(applicationPasswords, 0.0);
            AnchorPane.setRightAnchor(applicationPasswords, 0.0);
            root.getChildren().add(applicationPasswords);

            Button addNewApplication = new Button("Add New Application");
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
                        passwordManager.generatePassword(newApplication.get());
                        showApplicationPassword(newApplication.get());
                    }
                    else
                    {
                        Alert alert = new Alert(Alert.AlertType.WARNING, "Application already registered!", ButtonType.OK);
                        alert.showAndWait();
                    }
                }
            });

            AnchorPane.setLeftAnchor(addNewApplication, 0.0);
            AnchorPane.setBottomAnchor(addNewApplication, 0.0);
            root.getChildren().add(addNewApplication);

            Button changeMasterPassword = new Button("Change Master Password");
            changeMasterPassword.setOnAction(event ->
            {
                PasswordDialog passwordDialog = new PasswordDialog();
                passwordDialog.setHeaderText("Enter new master password");
                Optional<String> newMasterPassword = passwordDialog.showAndWait();
                if (newMasterPassword.isPresent())
                {
                    passwordManager.changeMasterPassword(newMasterPassword.get());
                }
            });

            AnchorPane.setRightAnchor(changeMasterPassword, 0.0);
            AnchorPane.setBottomAnchor(changeMasterPassword, 0.0);
            root.getChildren().add(changeMasterPassword);

            primaryStage.setTitle("Password Hero");
            primaryStage.setScene(new Scene(root, MIN_APPLICATION_WIDTH, MIN_APPLICATION_HEIGHT));
            primaryStage.setMinWidth(MIN_APPLICATION_WIDTH);
            primaryStage.setMinHeight(MIN_APPLICATION_HEIGHT);
            primaryStage.show();
        }
        catch (EncryptionOperationNotPossibleException e)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid password!", ButtonType.OK);
            alert.showAndWait();
        }
    }

    private Optional<String> getMasterPassword()
    {
        PasswordDialog passwordDialog = new PasswordDialog();
        passwordDialog.setHeaderText("Enter master password");
        return passwordDialog.showAndWait();
    }

    private void showApplicationPassword(String application)
    {
        String savedPassword = passwordManager.getPassword(application);
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

    private void deleteApplicationPassword(String application)
    {
        passwordManager.deletePassword(application);
    }

    private final class ApplicationPasswordCell extends ListCell<String>
    {
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null)
            {
                AnchorPane row = new AnchorPane();

                HBox appNameHBox = new HBox(5);

                Label applicationName = new Label(item);
                appNameHBox.getChildren().add(applicationName);
                AnchorPane.setTopAnchor(appNameHBox, 0.0);
                AnchorPane.setBottomAnchor(appNameHBox, 0.0);
                AnchorPane.setLeftAnchor(appNameHBox, 0.0);
                row.getChildren().add(appNameHBox);

                HBox controlsHBox = new HBox(5);

                Button show = new Button("show");
                show.setOnAction(event -> showApplicationPassword(item));

                Button change = new Button("change");
                change.setOnAction(event ->
                {
                    Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                        "Change password for " + item + "?", ButtonType.YES, ButtonType.NO);
                    confirmation.setTitle("Confirmation");
                    confirmation.setHeaderText("Confirm Password Change");
                    Optional<ButtonType> result = confirmation.showAndWait();
                    if (result.isPresent() && result.get().equals(ButtonType.YES))
                    {
                        passwordManager.changePassword(item);
                        showApplicationPassword(item);
                    }
                });

                Button delete = new Button("delete");
                delete.setOnAction(event ->
                {
                    Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                        "Delete password for " + item + "?", ButtonType.YES, ButtonType.NO);
                    confirmation.setTitle("Confirmation");
                    confirmation.setHeaderText("Confirm Deletion of Password");
                    Optional<ButtonType> result = confirmation.showAndWait();
                    if (result.isPresent() && result.get().equals(ButtonType.YES))
                    {
                        deleteApplicationPassword(item);
                    }
                });

                controlsHBox.getChildren().addAll(show, change, delete);
                AnchorPane.setTopAnchor(controlsHBox, 0.0);
                AnchorPane.setBottomAnchor(controlsHBox, 0.0);
                AnchorPane.setRightAnchor(controlsHBox, 0.0);
                row.getChildren().add(controlsHBox);

                setGraphic(row);
            }
            else
            {
                setGraphic(null);
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
