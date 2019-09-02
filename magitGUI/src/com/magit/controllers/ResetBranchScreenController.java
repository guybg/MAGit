package com.magit.controllers;

import com.magit.controllers.interfaces.BasicController;
import com.magit.controllers.interfaces.BasicPopupScreenController;
import com.magit.gui.PopupScreen;
import com.magit.logic.exceptions.*;
import com.magit.logic.system.MagitEngine;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.ResourceBundle;

public class ResetBranchScreenController implements BasicController , Initializable {

    @FXML private Label labelTitle;
    @FXML private Label keyLabel;
    @FXML private Button acceptButton;
    @FXML private Button cancelButton;
    @FXML private Label errorLabel;
    @FXML private ComboBox<String> comboBox;
    private Stage rootStage;
    private MagitEngine engine;
    private BasicPopupScreenController controller;
    private Tooltip comboToolTip;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comboToolTip= new Tooltip();
        comboToolTip.textProperty().bind(comboBox.valueProperty());
        comboBox.setTooltip(comboToolTip);
        acceptButton.setDisable(true);
        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> acceptButton.setDisable(false));
        labelTitle.setText("Change head pointed commit");
        keyLabel.setText("Commits");
    }

    @FXML
    void onComboBoxClicked(MouseEvent event) {
        try {
            errorLabel.setText("");
            comboBox.getItems().clear();
            comboBox.getItems().addAll(engine.guiGetAllCommitsOfRepository());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onAcceptClicked(ActionEvent event) {
        try {
            engine.activeBranchHasUnhandledMerge();
            engine.workingCopyChangedComparedToCommit();
            engine.changeBranchPointedCommit(comboBox.getValue());
            errorLabel.setText("Head branch pointed commit changed successfully.");
            acceptButton.setDisable(true);
        } catch (IOException | CommitNotFoundException | ParseException | RepositoryNotFoundException | PreviousCommitsLimitExceededException ignored) {
        } catch (UncommitedChangesException | UnhandledMergeException e) {
            PopupScreen popupScreen = new PopupScreen(((Stage)((Button)event.getSource()).getScene().getWindow()),engine);
            try {
                popupScreen.createNotificationPopup((BasicPopupScreenController) event1 -> {
                    try {
                        engine.changeBranchPointedCommit(comboBox.getValue());
                        errorLabel.setText("Head branch pointed commit changed successfully.");
                        acceptButton.setDisable(true);
                        (((Stage)((Button)event1.getSource()).getScene().getWindow())).close();
                    } catch (IOException | CommitNotFoundException | ParseException | RepositoryNotFoundException | PreviousCommitsLimitExceededException ignored) {
                    }
                },true,"Head branch change notification", "There are unsaved changes, switching pointed commit may cause lose of data.","Cancel");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
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
