package com.magit.controllers;

import com.magit.controllers.interfaces.BasicController;
import com.magit.controllers.interfaces.BasicPopupScreenController;
import com.magit.gui.PopupScreen;
import com.magit.logic.system.MagitEngine;
import com.magit.logic.system.tasks.ImportRepositoryTask;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;


public class ImportXmlNodeController implements BasicController {

    private Stage stage;
    private MagitEngine engine;
    private ImportRepositoryTask task;
    private File file;
    private StringProperty repositoryNameProperty;

    @FXML
    private Label currentStatus;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private AnchorPane pane;



    public void bindTaskToUi(ImportRepositoryTask task) {
        progressBar.progressProperty().bind(task.progressProperty());
        currentStatus.textProperty().bind(task.messageProperty());
    }

    public void start(boolean forceCreation){
        pane.setVisible(false);
        browseXml();
        if(file == null) return;
        pane.setVisible(true);
        importXml(forceCreation);
    }

    void importXml(boolean forceCreation) {
        task = new ImportRepositoryTask(file.getAbsolutePath(), engine, pane, repositoryNameProperty, new Runnable() {
            @Override
            public void run() {
                PopupScreen popupScreen = new PopupScreen(stage,engine);
                try {
                    popupScreen.createNotificationPopup(event -> {
                        task = new ImportRepositoryTask(file.getAbsolutePath(), engine, pane, repositoryNameProperty, null,true);
                        bindTaskToUi(task);
                        new Thread(task).start();
                        ((Stage)((Button)event.getSource()).getScene().getWindow()).close();
                    }, true,"Repository already exists notification","Would you like to replace current repository with XML repository?","Cancel");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, forceCreation);
        bindTaskToUi(task);

        new Thread(task).start();
    }

    void browseXml() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Xml files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);
        file = fileChooser.showOpenDialog(stage);
    }
    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void setEngine(MagitEngine engine) {
        this.engine = engine;
    }

    public void setRepositoryNameProperty(StringProperty repositoryNameProperty) {
        this.repositoryNameProperty = repositoryNameProperty;
    }
}
