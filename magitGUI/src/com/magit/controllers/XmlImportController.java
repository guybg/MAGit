package com.magit.controllers;

import com.magit.controllers.interfaces.BasicController;
import com.magit.logic.exceptions.IllegalPathException;
import com.magit.logic.exceptions.XmlFileException;
import com.magit.logic.system.MagitEngine;
import com.magit.logic.system.managers.ImportRepositoryTask;
import com.magit.logic.system.managers.RepositoryXmlParser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;

public class XmlImportController implements BasicController {

    private Stage stage;
    private MagitEngine engine;
    private ImportRepositoryTask task;

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

   // public XmlImportController(MagitEngine engine, Stage stage) {
   //     this.stage = stage;
   //     this.engine = engine;
   // }

    @FXML
    void onBrowseClicked(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Xml files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);
        file = fileChooser.showOpenDialog(stage);
    }

    public void bindTaskToUi(ImportRepositoryTask task) {
        progressBar.progressProperty().bind(task.progressProperty());
        statusLabel.textProperty().bind(task.messageProperty());
    }

    @FXML
    void onImportClicked(ActionEvent event) throws IOException, XmlFileException, IllegalPathException, JAXBException {
        task = new ImportRepositoryTask(file.getAbsolutePath(), engine, false);

        bindTaskToUi(task);

        new Thread(task).start();
    }

    @FXML
    void OnCancelClicked(ActionEvent event) {
        stage.close();
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
