package com.magit.controllers;


import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

import java.util.function.Consumer;

public class WelcomeNodeController {
    Runnable onCreate;
    Runnable onLoad;
    Runnable onLoadXml;

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
}
