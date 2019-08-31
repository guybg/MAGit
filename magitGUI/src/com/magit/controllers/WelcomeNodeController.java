package com.magit.controllers;


import com.magit.controllers.interfaces.BasicController;
import com.magit.logic.system.MagitEngine;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class WelcomeNodeController implements BasicController {
    Runnable onCreate;
    Runnable onLoad;
    Runnable onLoadXml;
    private Stage stage;
    private MagitEngine engine;

    @FXML
    void onCreateNewRepositoryWelcomeNode(MouseEvent event) {
        onCreate.run();
    }

    @FXML
    void onLoadRepositoryWelcomeNode(MouseEvent event) {
        onLoad.run();
    }

    @FXML
    void onLoadRepositoryXmlWelcomeNode(MouseEvent event) {
        onLoadXml.run();
    }

    public void setOnCreate(Runnable onCreate) {
        this.onCreate = onCreate;
    }

    public void setOnLoad(Runnable onLoad) {
        this.onLoad = onLoad;
    }

    public void setOnLoadXml(Runnable onLoadXml) {
        this.onLoadXml = onLoadXml;
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
