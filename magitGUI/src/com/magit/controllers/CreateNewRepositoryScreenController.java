package com.magit.controllers;

import com.magit.controllers.interfaces.BasicController;
import com.magit.gui.utils.BrowseHandler;
import com.magit.logic.exceptions.IllegalPathException;
import com.magit.logic.exceptions.InvalidNameException;
import com.magit.logic.exceptions.RepositoryAlreadyExistsException;
import com.magit.logic.system.MagitEngine;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class CreateNewRepositoryScreenController implements BasicController, Initializable {

    private Stage stage;
    private MagitEngine engine;
    private StringProperty repositoryNameProperty;
    private StringProperty repositoryPathProperty;
    @FXML
    private Button closeButton;

    @FXML
    private TextField newRepositoryNameTextField;

    @FXML
    private TextField browsePathTextField;

    @FXML
    private Button createNewRepositoryButton;

    @FXML
    private Button browseButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Label errorLabel;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setEngine(MagitEngine engine) {
        this.engine = engine;
    }

    public void setRepositoryNameProperty(StringProperty repositoryName) {
        this.repositoryNameProperty = repositoryName;
    }

    public void setRepositoryPathProperty(StringProperty repositoryPathProperty) {
        this.repositoryPathProperty = repositoryPathProperty;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void bindings(){

    }
    @FXML
    void closeAction(MouseEvent event) {
        Button closeButton = (Button)event.getSource();
        Stage curStage = (Stage) closeButton.getScene().getWindow();
        curStage.close();
    }

    @FXML
    void createNewRepositoryAction(MouseEvent event) {
        try {
            engine.createNewRepository(Paths.get(browsePathTextField.getText()), newRepositoryNameTextField.getText());
            errorLabel.setText("Repository created successfully!");
            repositoryNameProperty.setValue(engine.getRepositoryName());
            repositoryPathProperty.setValue(engine.guiGetRepositoryPath());
            Button closeButton = (Button)event.getSource();
            closeButton.setDisable(true);
        } catch (IllegalPathException | InvalidNameException | RepositoryAlreadyExistsException e) {
            errorLabel.setText(e.getMessage());
        }
    }

    @FXML
    void browseAction(MouseEvent event) {
        BrowseHandler.browseFolder((Button) event.getSource(), browsePathTextField);
    }

}
