package com.magit.controllers;

import com.magit.animations.PulseTransition;
import com.magit.logic.visual.node.CommitNode;
import com.sun.org.apache.xml.internal.security.Init;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public class CommitNodeController implements Initializable {

    @FXML private Label commitTimeStampLabel;
    @FXML private Label messageLabel;
    @FXML private Label committerLabel;
    @FXML private Label activeBranchLabel;
    @FXML private Circle CommitCircle;
    @FXML private ScrollPane scrollPaneContainer;
    @FXML private GridPane gridPane;
    private boolean focused = false;

    private String sha1;
    private String parent1Sha1;
    private String parent2Sha1;
    private HashMap<String, CommitNode> parents;
    private ArrayList<String> branches;

    private BranchesHistoryScreenController branchesHistoryScreenController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setCommitTimeStamp(String timeStamp) {
        commitTimeStampLabel.setText(timeStamp);
        commitTimeStampLabel.setTooltip(new Tooltip(timeStamp));
    }

    public void setBranchesHistoryScreenController(BranchesHistoryScreenController branchesHistoryScreenController) {
        this.branchesHistoryScreenController = branchesHistoryScreenController;
        branchesHistoryScreenController.scrollPaneContainer.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(focused)
                gridPane.requestFocus();
        });
        branchesHistoryScreenController.focusChanged.addListener((observable, oldValue, newValue) -> {
            if(focused) focused = false;
        });
    }

    public void setBranches(ArrayList<String> branches) {
        this.branches = branches;
    }

    public void setCommitter(String committerName) {
        committerLabel.setText(committerName);
        committerLabel.setTooltip(new Tooltip(committerName));
    }

    public void setCommitMessage(String commitMessage) {
        messageLabel.setText(commitMessage);
        messageLabel.setTooltip(new Tooltip(commitMessage));
    }
    public void setActiveBranch(String activeBranch) {
        activeBranchLabel.setText("[" + activeBranch + "]");
        activeBranchLabel.setTooltip(new Tooltip(activeBranch));
        CommitCircle.setFill(Color.YELLOW);
    }
    public int getCircleRadius() {
        return (int)CommitCircle.getRadius();
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    public void setParent1Sha1(String parent1Sha1) {
        this.parent1Sha1 = parent1Sha1;
    }

    public void setParent2Sha1(String parent2Sha1) {
        this.parent2Sha1 = parent2Sha1;
    }

    public void setParents(HashMap<String, CommitNode> parents) {
        this.parents = parents;
    }

    public void onCommitClicked(MouseEvent mouseEvent) {
        branchesHistoryScreenController.setCurCommitSha1Label(sha1);
        branchesHistoryScreenController.setCommitMessageLabel(messageLabel.getText());
        branchesHistoryScreenController.setLastCommit1HyperLink(parent1Sha1);
        branchesHistoryScreenController.setLastCommit2HyperLink(parent2Sha1);
        branchesHistoryScreenController.setCreatorLabel(committerLabel.getText());
        branchesHistoryScreenController.setCreationDateLabel(commitTimeStampLabel.getText());
        branchesHistoryScreenController.setLastCommit1Node(parents.get(parent1Sha1));
        branchesHistoryScreenController.setLastCommit2Node(parents.get(parent2Sha1));
        branchesHistoryScreenController.setAllBranchesLabel(String.join(", ", branches), branches.size());
        PulseTransition pulseTransition= new PulseTransition(gridPane);
        pulseTransition.play();
        gridPane.requestFocus();
        branchesHistoryScreenController.focusChanged.setValue(!branchesHistoryScreenController.focusChanged.getValue());
        focused = true;
    }
}
