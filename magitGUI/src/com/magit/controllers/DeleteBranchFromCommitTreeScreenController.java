package com.magit.controllers;

import com.magit.controllers.interfaces.BasicController;
import com.magit.logic.exceptions.*;
import com.magit.logic.system.MagitEngine;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class DeleteBranchFromCommitTreeScreenController implements BasicController {

    private Stage stage;
    private MagitEngine engine;
    private ArrayList<String> branches;
    @FXML
    private ComboBox<String> comboBox;

    @FXML
    private Button acceptButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Label messageLabel;

    @FXML
    void onAccept(ActionEvent event) {
        try {
            engine.deleteBranch(comboBox.getSelectionModel().getSelectedItem());
            messageLabel.setText("Branch deleted Successfully");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ActiveBranchDeletedException | RepositoryNotFoundException |BranchNotFoundException | RemoteBranchException e) {
            messageLabel.setText(e.getMessage());
        } catch (BranchDeletedRemotelyException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onCancel(ActionEvent event) {
        ((Stage)((Button)event.getSource()).getScene().getWindow()).close();
    }

    @FXML
    void onComboBoxClicked(MouseEvent event) {
        comboBox.getItems().clear();
        comboBox.getItems().addAll(branches);
    }

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void setEngine(MagitEngine engine) {
        this.engine = engine;
    }

    public void setBranches(ArrayList<String> branches) {
        this.branches = branches;
    }
}
