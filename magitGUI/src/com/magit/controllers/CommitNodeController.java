package com.magit.controllers;

import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.graph.Model;
import com.fxgraph.graph.PannableCanvas;
import com.magit.animations.PulseTransition;
import com.magit.gui.PopupScreen;
import com.magit.logic.exceptions.*;
import com.magit.logic.system.objects.Branch;
import com.magit.logic.visual.layout.CommitTreeLayout;
import com.magit.logic.visual.node.CommitNode;
import com.sun.org.apache.xml.internal.security.Init;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;


public class CommitNodeController implements Initializable {

    @FXML private Label commitTimeStampLabel;
    @FXML private Label messageLabel;
    @FXML private Label committerLabel;
    @FXML private Label activeBranchLabel;
    @FXML private Circle CommitCircle;
    @FXML private ScrollPane scrollPaneContainer;
    @FXML private GridPane gridPane;
    @FXML private MenuItem newBranchButton;
    @FXML private MenuItem resetHeadButton;
    @FXML private MenuItem mergeWithHeadButton;
    @FXML private MenuItem deleteBranchButton;

    private boolean focused = false;

    private String sha1;
    private String parent1Sha1;
    private String parent2Sha1;
    private HashMap<String, CommitNode> parents;
    private ArrayList<String> branches;
    private ArrayList<String> activeBranches;
    private BranchesHistoryScreenController branchesHistoryScreenController;
    private StringProperty clickedActiveBranches;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void setClickedActiveBranches(StringProperty clickedActiveBranches) {
        this.clickedActiveBranches = clickedActiveBranches;
        clickedActiveBranches.addListener((observable, oldValue, newValue) -> {
            //gridPane.getStyleClass().remove("marked-node");
            gridPane.getStyleClass().clear();
            gridPane.getStyleClass().add("single-commit-row-container");
            if (null == branches || branches.isEmpty())
                return;
            for (String branchName : branches) {
                if (Arrays.asList(clickedActiveBranches.getValue().split(",")).contains(branchName))
                    gridPane.getStyleClass().add("marked-node");
            }
        });
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
    public void setActiveBranch(HashSet<Branch> activeBranches) {
        this.activeBranches = activeBranches.stream().map(Branch::getBranchName).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<String> activeBranchesToolTipArray = activeBranches.stream().map(Branch::getBranchName).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<String> activeBranchesLabelArray = activeBranches.stream().map(b -> "[" + b.getBranchName() + "]").collect(Collectors.toCollection(ArrayList::new));
        activeBranchLabel.setText(String.join(" | ", activeBranchesLabelArray));
        activeBranchLabel.setTooltip(new Tooltip(String.join(", ", activeBranchesToolTipArray)));
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
        if (null != activeBranches)
            clickedActiveBranches.setValue(String.join(",", activeBranches));
        else
            clickedActiveBranches.setValue(" ");
    }

    private void showErrorMessage(String message){
        PopupScreen popupScreen = new PopupScreen((Stage)messageLabel.getScene().getWindow(),branchesHistoryScreenController.getEngine());
        popupScreen.showErrorMessage(message);
    }

    @FXML
    void onDeletePointingBranch(ActionEvent event) throws IOException, ParseException, PreviousCommitsLimitExceededException {
        if(activeBranches == null) {
            showErrorMessage("There are no branches to delete on selected commit");
            return;
        }
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/com/magit/resources/fxml/deleteBranchFromCommitTreeScreen.fxml"));
        Parent layout = loader.load();
        ((DeleteBranchFromCommitTreeScreenController)loader.getController()).setBranches(activeBranches);
        PopupScreen popupScreen = new PopupScreen(((Stage)activeBranchLabel.getScene().getWindow()),branchesHistoryScreenController.getEngine());
        popupScreen.createPopup(layout,loader.getController());
        updateGraph();
    }

    @FXML
    void onMergeWithHead(ActionEvent event) throws IOException, ParseException, PreviousCommitsLimitExceededException {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/magit/resources/fxml/mergeCommitTreeScreen.fxml"));
            Parent layout = loader.load();
            MergeCommitTreeScreenController mergeScreenController = loader.getController();
            mergeScreenController.setEngine(branchesHistoryScreenController.getEngine());
            mergeScreenController.setStage(((Stage)activeBranchLabel.getScene().getWindow()));
            mergeScreenController.setBranchesAtCommit(branchesHistoryScreenController.getEngine().getNonRemoteBranchesOfCommit(sha1));
            PopupScreen popupScreen = new PopupScreen(((Stage)activeBranchLabel.getScene().getWindow()),branchesHistoryScreenController.getEngine());
            popupScreen.createPopup(layout, loader.getController());
            updateGraph();
        } catch (BranchNotFoundException e) {
            showErrorMessage(e.getMessage());
        }
    }

    @FXML
    void onNewBranch(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/com/magit/resources/fxml/createBranchScreen.fxml"));
        Parent layout = loader.load();
        CreateBranchScreenController changeBranchController =
               loader.getController();
        changeBranchController.setEngine(branchesHistoryScreenController.getEngine());
        changeBranchController.setStage(branchesHistoryScreenController.getStage());
        changeBranchController.setSha1OfCommit(sha1);
        changeBranchController.setRefreshGraph(() -> {
            try {
                updateGraph();
            } catch (ParseException | PreviousCommitsLimitExceededException | IOException e) {
                e.printStackTrace();
            }
        });
        PopupScreen popupScreen = new PopupScreen(((Stage)activeBranchLabel.getScene().getWindow()),branchesHistoryScreenController.getEngine());
        popupScreen.createPopup(layout,changeBranchController);
    }

    @FXML
    void onResetHead(ActionEvent event) {
        try {
            branchesHistoryScreenController.getEngine().changeBranchPointedCommit(sha1);
            updateGraph();
        } catch (IOException | PreviousCommitsLimitExceededException | RepositoryNotFoundException | ParseException | CommitNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void updateGraph() throws ParseException, PreviousCommitsLimitExceededException, IOException {
        Graph graph = new Graph();
        Model model = graph.getModel();

        TreeSet<CommitNode> nodes = branchesHistoryScreenController.getEngine().guiBranchesHistory(model,branchesHistoryScreenController);
        graph.beginUpdate();
        for(ICell node : nodes) {
            if(!model.getAllCells().contains(node))
                model.addCell(node);
        }
        graph.endUpdate();
        graph.layout(new CommitTreeLayout());
        branchesHistoryScreenController.scrollPaneContainer.setContent(graph.getCanvas());
        Platform.runLater(() -> {
            graph.getUseViewportGestures().set(false);
            graph.getUseNodeGestures().set(false);
        });
    }
}
