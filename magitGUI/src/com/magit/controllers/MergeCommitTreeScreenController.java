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
    void onAccept(ActionEvent event) throws IOException {
        try {
            engine.merge(comboBox.getSelectionModel().getSelectedItem(),false);
            showMerge();
            ((Stage)acceptButton.getScene().getWindow()).close();
        } catch (MergeNotNeededException | MergeException e) {
            showErrorMessage(e.getMessage());
            ((Stage)acceptButton.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FastForwardException e) {
            showErrorMessage(e.getMessage());
            showMerge();
        } catch (UnhandledMergeException e) {
            showErrorMessage("Please solve unhandled merge before this operation.");
            ((Stage)acceptButton.getScene().getWindow()).close();
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
        PopupScreen popupScreen = new PopupScreen((Stage)messageLabel.getScene().getWindow(),engine);
        popupScreen.showErrorMessage(message);
    }

    public void setBranchesAtCommit(ArrayList<String> branchesAtCommit) {
        this.branchesAtCommit = branchesAtCommit;
    }

    private void showMerge() throws IOException {
        PopupScreen popupScreen = new PopupScreen(((Stage)acceptButton.getScene().getWindow()),engine);
        popupScreen.createMergeScreenWithPreChosenBranch();
    }
}
