package com.magit.controllers;

import com.magit.controllers.interfaces.BasicController;
import com.magit.logic.system.MagitEngine;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class CommitNodeController {

    @FXML private Label commitTimeStampLabel;
    @FXML private Label messageLabel;
    @FXML private Label committerLabel;
    @FXML private Label activeBranchLabel;
    @FXML private Circle CommitCircle;

    public void setCommitTimeStamp(String timeStamp) {
        commitTimeStampLabel.setText(timeStamp);
        commitTimeStampLabel.setTooltip(new Tooltip(timeStamp));
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
        activeBranchLabel.setText(activeBranch);
        messageLabel.setTooltip(new Tooltip(activeBranch));
        CommitCircle.setFill(Color.YELLOW);
    }
    public int getCircleRadius() {
        return (int)CommitCircle.getRadius();
    }

}
