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
package com.rtoth.password.standalone.ui;

import com.google.common.base.Preconditions;
import com.rtoth.password.core.PasswordManager;

import org.jasypt.exceptions.EncryptionOperationNotPossibleException;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Main application entry point.
 */
public class ApplicationController extends Application
{
    /** Minimum width of the application. */
    private static final double MIN_APPLICATION_WIDTH = 300.0;

    /** Minimum height of the application. */
    private static final double MIN_APPLICATION_HEIGHT = 250.0;

    /** Maximum number of seconds to display a password before closing the dialog. */
    private static final double PASSWORD_VIEW_DURATION_SECS = 30.0;

    /** Static location of the passwords file in the user's home directory. */
    private static final String FILE_SUFFIX = ".password-hero" + File.separator + "encrypted.gpg";

    private final ObservableList<String> allApplications = FXCollections.observableArrayList();

    /** Manages all password-related operations. */
    private PasswordManager passwordManager;

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        Optional<File> passwordFile = getPasswordFileLocation();
        if (passwordFile.isPresent())
        {
            Optional<String> masterPassword;
            // First time login -- perform initial setup and whatnot
            if (!passwordFile.get().exists())
            {
                masterPassword = performInitialSetup(passwordFile.get());
            }
            else
            {
                PasswordDialog masterPasswordDialog = new PasswordDialog();
                masterPasswordDialog.setHeaderText("Enter master password");
                masterPassword = masterPasswordDialog.showAndWait();
            }

            if (masterPassword.isPresent())
            {
                try
                {
                    passwordManager = new PasswordManager(passwordFile.get(), masterPassword.get());
                    allApplications.addAll(passwordManager.getAvailableApplications());

                    Font.loadFont(
                        ApplicationController.class.getResource("/fonts/fontawesome-webfont.ttf").toExternalForm(),
                        12);


                    AnchorPane root = new AnchorPane();
                    root.setPrefWidth(MIN_APPLICATION_WIDTH);
                    root.setMinWidth(MIN_APPLICATION_WIDTH);
                    root.setPrefHeight(MIN_APPLICATION_HEIGHT);
                    root.setMinHeight(MIN_APPLICATION_HEIGHT);

                    final ListView<String> applicationPasswords = new ListView<>(allApplications);
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
                                allApplications.add(newApplication.get());
                                showApplicationPassword(newApplication.get());
                            }
                            else
                            {
                                Alert alert = new Alert(Alert.AlertType.WARNING,
                                    "Application already registered!", ButtonType.OK);
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
                        PasswordDialog newPasswordDialog = new PasswordDialog();
                        newPasswordDialog.setHeaderText("Enter new master password");
                        Optional<String> newMasterPassword = newPasswordDialog.showAndWait();
                        if (newMasterPassword.isPresent())
                        {
                            passwordManager.changeMasterPassword(newMasterPassword.get());
                        }
                    });

                    AnchorPane.setRightAnchor(changeMasterPassword, 0.0);
                    AnchorPane.setBottomAnchor(changeMasterPassword, 0.0);
                    root.getChildren().add(changeMasterPassword);

                    Scene scene = new Scene(root, MIN_APPLICATION_WIDTH, MIN_APPLICATION_HEIGHT);
                    scene.getStylesheets().add(
                        ApplicationController.class.getResource("/css/application.css").toExternalForm());

                    primaryStage.setTitle("Password Hero");
                    primaryStage.setScene(scene);
                    primaryStage.setMinWidth(MIN_APPLICATION_WIDTH);
                    primaryStage.setMinHeight(MIN_APPLICATION_HEIGHT);
                    primaryStage.show();

                    // FIXME: Why does the application not shut down properly?
                }
                catch (EncryptionOperationNotPossibleException e)
                {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid password!", ButtonType.OK);
                    alert.showAndWait();
                }
            }
        }
        else
        {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error retrieving user home directory.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    /**
     * Get the location of the password file for the current user.
     * @return An {@link Optional} containing the expected password file location for the current user, or
     *         {@link Optional#empty()} if there is a problem accessing the user's home directory.
     */
    private Optional<File> getPasswordFileLocation()
    {
        Optional<File> passwordFile = Optional.empty();

        String userHome = System.getProperty("user.home");
        if (userHome != null)
        {
            passwordFile = Optional.of(
                new File(userHome + File.separator + FILE_SUFFIX)
            );
        }

        return passwordFile;
    }

    /**
     * Perform initial application setup for users that launch for the first time.
     *
     * @param passwordFile File which will be created if necessary to house encrypted passwords. Cannot be {@code null}
     *                     and must not exist ({@link File#exists()}) .
     * @return An {@link Optional} containing the user's initial plaintext master password if setup is successful,
     *         {@link Optional#empty()} otherwise. Never {@code null}.
     *
     * @throws NullPointerException if {@code passwordFile} is {@code null}.
      */
    private Optional<String> performInitialSetup(File passwordFile)
    {
        Preconditions.checkNotNull(passwordFile, "passwordFile cannot be null.");
        Preconditions.checkArgument(!passwordFile.exists(), "passwordFile already exists.");

        PasswordDialog masterPasswordDialog = new PasswordDialog();
        masterPasswordDialog.setTitle("Password Hero");
        masterPasswordDialog.setHeaderText(
            "Welcome to password hero!\nPlease enter a master password to get started.");
        Optional<String> masterPassword = masterPasswordDialog.showAndWait();
        if (masterPassword.isPresent())
        {
            boolean creationResult = true;
            try
            {
                File parent = passwordFile.getParentFile();
                if (parent != null && !parent.exists())
                {
                    creationResult = parent.mkdirs();
                }
                creationResult &= passwordFile.createNewFile();
            }
            catch (IOException ioe)
            {
                creationResult = false;
            }

            if (!creationResult)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Unable to create password file store at " + passwordFile.getAbsolutePath(), ButtonType.OK);
                alert.showAndWait();
                masterPassword = Optional.empty();
            }
        }

        return masterPassword;
    }

    /**
     * Show the current plaintext password for the provided application.
     *
     * @param application Application for which to show the current plaintext password. Cannot be {@code null}.
     *
     * @throws NullPointerException if {@code application} is {@code null}.
     */
    private void showApplicationPassword(String application)
    {
        String savedPassword = passwordManager.getPlaintextPassword(application);
        if (savedPassword != null)
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, savedPassword, ButtonType.OK);
            alert.setTitle("Saved password");
            alert.setHeaderText("Saved password for " + application + "");

            VBox root = new VBox(5);

            TextField savedPasswordText = new TextField(savedPassword);
            savedPasswordText.setEditable(false);
            HBox.setHgrow(savedPasswordText, Priority.ALWAYS);

            HBox passwordHBox = new HBox();
            passwordHBox.prefWidthProperty().bind(root.widthProperty());
            passwordHBox.setPadding(new Insets(5));
            passwordHBox.getChildren().add(savedPasswordText);

            ProgressBar progressBar = new ProgressBar(0);
            progressBar.prefWidthProperty().bind(root.widthProperty());
            progressBar.setPrefHeight(24);
            progressBar.setPadding(new Insets(5));

            root.getChildren().addAll(progressBar, passwordHBox);

            Timeline timeline = new Timeline(
                new KeyFrame(
                    Duration.ZERO,
                    new KeyValue(progressBar.progressProperty(), 0)
                ),
                new KeyFrame(
                    Duration.seconds(PASSWORD_VIEW_DURATION_SECS),
                    event -> alert.close(),
                    new KeyValue(progressBar.progressProperty(), 1)
                )
            );

            alert.getDialogPane().setContent(root);
            alert.show();
            alert.setOnCloseRequest(e -> timeline.stop());
            timeline.playFromStart();
        }
    }

    /**
     * List cell for a single application which contains controls to show, update, and delete its password.
     */
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

                Button show = FontAwesomeUtility.createIconButton(FontAwesomeIcon.OPEN_EYE);
                show.setOnAction(event -> showApplicationPassword(item));

                Button change = FontAwesomeUtility.createIconButton(FontAwesomeIcon.REFRESH);
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

                Button delete = FontAwesomeUtility.createIconButton(FontAwesomeIcon.TRASH);
                delete.setOnAction(event ->
                {
                    Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                        "Delete password for " + item + "?", ButtonType.YES, ButtonType.NO);
                    confirmation.setTitle("Confirmation");
                    confirmation.setHeaderText("Confirm Deletion of Password");
                    Optional<ButtonType> result = confirmation.showAndWait();
                    if (result.isPresent() && result.get().equals(ButtonType.YES))
                    {
                        passwordManager.deletePassword(item);
                        allApplications.remove(item);
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
