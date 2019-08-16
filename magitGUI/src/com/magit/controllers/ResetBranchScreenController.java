package com.magit.controllers;

import com.magit.controllers.interfaces.BasicController;
import com.magit.controllers.interfaces.BasicPopupScreenController;
import com.magit.logic.system.MagitEngine;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class ResetBranchScreenController implements BasicController {

    @FXML private Label labelTitle;
    @FXML private Label keyLabel;
    @FXML private Button acceptButton;
    @FXML private Button cancelButton;
    @FXML private Label errorLabel;
    @FXML private ComboBox<String> comboBox;
    private Stage rootStage;
    private MagitEngine engine;
    private BasicPopupScreenController controller;

    @FXML
    void onAcceptClicked(ActionEvent event) {
        controller.onAccept(event);
        try {
            comboBox.getItems().addAll(engine.guiGetAllCommitsOfRepository());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @FXML
    void onCancelClicked(ActionEvent event) {
        Button chosen = (Button)event.getSource();
        Stage curStage = (Stage) chosen.getScene().getWindow();
        curStage.close();
    }

    @Override
    public void setStage(Stage stage) {
        rootStage = stage;
    }

    @Override
    public void setEngine(MagitEngine engine) {
        this.engine = engine;
    }
}
