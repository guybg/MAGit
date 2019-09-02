package com.magit.controllers;

import com.magit.controllers.interfaces.BasicController;
import com.magit.gui.PopupScreen;
import com.magit.logic.exceptions.*;
import com.magit.logic.system.MagitEngine;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class MergeCommitTreeScreenController implements BasicController {
    private MagitEngine engine;
    private Stage stage;

    @FXML
    private ComboBox<String> comboBox;

    @FXML
    private Button acceptButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Label messageLabel;

    private ArrayList<String> branchesAtCommit;

    @FXML
    void onAccept(ActionEvent event) {
        try {
            engine.merge(comboBox.getSelectionModel().getSelectedItem(),false);
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/magit/resources/fxml/mergeScreen.fxml"));
            Parent layout = loader.load();
            MergeScreenController mergeScreenController = loader.getController();
            mergeScreenController.setEngine(engine);
            mergeScreenController.setStage(((Stage)acceptButton.getScene().getWindow()));
            mergeScreenController.preReadyMerge();
            PopupScreen popupScreen = new PopupScreen(((Stage)acceptButton.getScene().getWindow()),engine);
            popupScreen.createPopup(layout, loader.getController());
            ((Stage)acceptButton.getScene().getWindow()).close();
        } catch (UnhandledMergeException | MergeNotNeededException | FastForwardException | MergeException e) {
            showErrorMessage(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onCancel(ActionEvent event) {
        ((Stage)cancelButton.getScene().getWindow()).close();
    }

    @FXML
    void onComboBoxClicked(MouseEvent event) {
        comboBox.getItems().clear();
        comboBox.getItems().addAll(branchesAtCommit);
    }


    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void setEngine(MagitEngine engine) {
        this.engine = engine;
    }
    private void showErrorMessage(String message){
        try {
            PopupScreen popupScreen = new PopupScreen((Stage)messageLabel.getScene().getWindow(),engine);
            popupScreen.createNotificationPopup(null,false,"Oops.. something went wrong.", message,"Close");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void setBranchesAtCommit(ArrayList<String> branchesAtCommit) {
        this.branchesAtCommit = branchesAtCommit;
    }
}
