package com.magit.controllers;

import com.magit.controllers.interfaces.BasicController;
import com.magit.gui.utils.BrowseHandler;
import com.magit.logic.system.MagitEngine;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class CloneScreenController implements BasicController {
    private Stage stage;
    private MagitEngine engine;
    @FXML
    private TextField destinationLocationTextField;

    @FXML
    private Button destinationBrowseButton;

    @FXML
    private TextField sourceLocationTextField;

    @FXML
    private Button sourceBrowseButton;

    @FXML
    private Button cloneButton;

    @FXML
    private Button cancelButton;

    @FXML
    void onCancel(ActionEvent event) {
        ((Stage)((Button)event.getSource()).getScene().getWindow()).close();
    }

    @FXML
    void onClone(ActionEvent event) {

    }

    @FXML
    void onDestinationBrowseButtonClicked(ActionEvent event) {
        BrowseHandler.browseFolder((Button)event.getSource(), destinationLocationTextField);
    }


    @FXML
    void onSourceBrowseButtonClicked(ActionEvent event) {
        BrowseHandler.browseFolder((Button)event.getSource(), sourceLocationTextField);
    }


    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void setEngine(MagitEngine engine) {
        this.engine = engine;
    }
}