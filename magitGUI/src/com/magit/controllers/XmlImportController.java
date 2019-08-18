package com.magit.controllers;

import com.magit.controllers.interfaces.BasicController;
import com.magit.logic.system.MagitEngine;
import com.magit.logic.system.managers.ImportRepositoryTask;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class XmlImportController implements BasicController {

    private Stage stage;
    private MagitEngine engine;

    @FXML
    private AnchorPane anchor;

    @FXML
    private Button browseButton;

    @FXML
    private Button importButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Label fileLabel;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label statusLabel;

    @FXML
    private Label currentStatus;

    private File file;

    @FXML
    void onBrowseClicked(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Xml files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);
        file = fileChooser.showOpenDialog(stage);
    }

    public String getFilePath() {
        return file.getAbsolutePath();
    }

    public void bindTaskToUi(ImportRepositoryTask task) {
        progressBar.progressProperty().bind(task.progressProperty());
        statusLabel.textProperty().bind(task.messageProperty());
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
