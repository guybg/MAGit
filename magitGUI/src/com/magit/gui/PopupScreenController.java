package com.magit.gui;

import com.magit.logic.system.MagitEngine;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;


public class PopupScreenController implements BasicController{
    private MagitEngine engine;
    private Stage stage;
    private BasicPopupScreenController controller;

    @FXML
    private Label popupHeadMessageLable;

    @FXML
    private Label popupBodyMessageLabel;

    @FXML
    private Button popupAcceptButton;

    @FXML
    private Button popupCancelButton;

    public void setPopupHeadMessageLableText(String message) {
        this.popupHeadMessageLable.setText(message);
    }

    public void setPopupBodyMessageLabel(String message) {
        this.popupBodyMessageLabel.setText(message);
    }

    public void setPopupAcceptButtonVisibility(boolean isVisible) {
        this.popupAcceptButton.setVisible(isVisible);
    }

    public void setPopupAcceptButtonText(String text){
        this.popupAcceptButton.setText(text);
    }

    public void setPopupCancelButtonText(String text){
        this.popupCancelButton.setText(text);
    }

    @FXML
    void onAccept(ActionEvent event) {
        controller.onAccept(event);
    }

    @FXML
    void onCancel(ActionEvent event) {
        Button chosen = (Button)event.getSource();
        Stage curStage = (Stage) chosen.getScene().getWindow();
        curStage.close();
    }

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void setEngine(MagitEngine engine) {
        this.engine = engine;
    }

    public void setController(BasicPopupScreenController controller) {
        this.controller = controller;
    }
}
