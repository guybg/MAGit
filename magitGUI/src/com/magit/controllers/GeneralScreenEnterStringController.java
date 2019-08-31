package com.magit.controllers;

import com.magit.controllers.interfaces.BasicController;
import com.magit.controllers.interfaces.BasicPopupScreenController;
import com.magit.logic.system.MagitEngine;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


public class GeneralScreenEnterStringController implements BasicController {

    private BasicPopupScreenController controller;

    @FXML
    private Label labelTitle;

    @FXML
    private Label keyLabel;

    @FXML
    private TextField valueTextField;

    @FXML
    private Button acceptButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Label errorLabel;

    @FXML
    private CheckBox checkBox;

    @FXML
    void onAcceptClicked(ActionEvent event) {
        controller.onAccept(event);
    }

    @FXML
    void onCancelClicked(ActionEvent event) {
        Button chosen = (Button) event.getSource();
        Stage curStage = (Stage) chosen.getScene().getWindow();
        curStage.close();
    }

    @Override
    public void setStage(Stage stage) {

    }

    @Override
    public void setEngine(MagitEngine engine) {

    }

    void setHeadLabel(String headText) {
        labelTitle.setText(headText);
    }

    void setKeyLabel(String keyText) {
        keyLabel.setText(keyText);
    }

    public void setController(BasicPopupScreenController controller) {
        this.controller = controller;
    }

    String getTextFieldValue() {
        return valueTextField.getText();
    }

    void setError(String error) {
        errorLabel.setText(error);
    }

    void setCheckBoxVisible(){
        checkBox.setVisible(true);
    }

    boolean getCheckBoxValue() {
        return checkBox.isSelected();
    }

    public void acceptButtonDisabled(boolean disabled) {
        this.acceptButton.setDisable(disabled);
    }
}