package com.magit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;


public class CommitNodeController {

    @FXML private Label commitTimeStampLabel;
    @FXML private Label messageLabel;
    @FXML private Label committerLabel;
    @FXML private Label activeBranchLabel;
    @FXML private Circle CommitCircle;
    @FXML private ScrollPane scrollPaneContainer;
    private String sha1;
    private String parent1Sha1;
    private String parent2Sha1;

    private BranchesHistoryScreenController branchesHistoryScreenController;
    public void setCommitTimeStamp(String timeStamp) {
        commitTimeStampLabel.setText(timeStamp);
        commitTimeStampLabel.setTooltip(new Tooltip(timeStamp));
    }

    public void setBranchesHistoryScreenController(BranchesHistoryScreenController branchesHistoryScreenController) {
        this.branchesHistoryScreenController = branchesHistoryScreenController;
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
        messageLabel.setTooltip(new Tooltip(activeBranch));
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

    public void onCommitClicked(MouseEvent mouseEvent) {
        branchesHistoryScreenController.setCommitMessageLabel(messageLabel.getText());
        branchesHistoryScreenController.setLastCommit1Label(parent1Sha1);
        branchesHistoryScreenController.setLastCommit2Label(parent2Sha1);
        branchesHistoryScreenController.setCreatorLabel(committerLabel.getText());
        branchesHistoryScreenController.setCurCommitSha1Label(sha1);
        branchesHistoryScreenController.showDifferencesBetweenCommitAndFirstParent();
    }
}
