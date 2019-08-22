package com.magit.logic.visual.node;

import com.fxgraph.cells.AbstractCell;
import com.fxgraph.edges.Edge;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.graph.IEdge;
import com.magit.controllers.BranchesHistoryScreenController;
import com.magit.controllers.CommitNodeController;
import com.magit.logic.system.objects.Branch;
import com.magit.logic.system.objects.Commit;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;

public class CommitNode extends AbstractCell implements Comparable<CommitNode>{
    private Commit commit;
    private Date timestamp;
    private String committer;
    private String message;
    private CommitNodeController commitNodeController;
    private String sha1;
    private String parent1Sha1;
    private String parent2Sha1;
    private BranchesHistoryScreenController branchesHistoryScreenController;

    private Integer posX;
    private Integer posY;

    private Branch activeBranch;
    private HashSet<Branch> branches = new HashSet<>();
    private boolean alreadySet = false;
    private SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss:SSS");


    public boolean isAlreadySet() {
        return alreadySet;
    }

    public void setAlreadySet(boolean alreadySet) {
        this.alreadySet = alreadySet;
    }

    public Branch getActiveBranch() {
        return activeBranch;
    }

    public void setActiveBranch(Branch activeBranch) {
        this.activeBranch = activeBranch;
    }

    public HashSet<Branch> getBranches() {
        return branches;
    }

    public void addBranch(Branch branch) {
        this.branches.add(branch);
    }

    public void setPosY(Integer posY) {
        this.posY = posY;
    }

    public Integer getPosY() {
        return posY;
    }
    @Override
    public int compareTo(CommitNode o) {
        return this.getDate().compareTo(o.getDate());
    }

    public CommitNode(Commit commit, BranchesHistoryScreenController branchesHistoryScreenController) {
        this.timestamp = commit.getCreationDate();
        this.committer = commit.getLastUpdater();
        this.message = commit.getCommitMessage();
        this.commit = commit;
        this.sha1 = commit.getSha1();
        this.parent1Sha1 = commit.getFirstPrecedingSha1();
        this.parent2Sha1 = commit.getSecondPrecedingSha1();
        this.branchesHistoryScreenController = branchesHistoryScreenController;
    }

    public Integer getPos() {
        return posX;
    }

    public void setPos(int pos) {
        this.posX = pos;
    }
    @Override
    public Region getGraphic(Graph graph) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource("/com/magit/resources/commitNode.fxml");
            fxmlLoader.setLocation(url);
            GridPane root = fxmlLoader.load(url.openStream());
            commitNodeController = fxmlLoader.getController();
            commitNodeController.setCommitMessage(message);
            commitNodeController.setCommitter(committer);
            commitNodeController.setCommitTimeStamp(formatter.format(timestamp));
            commitNodeController.setParent1Sha1(parent1Sha1);
            commitNodeController.setParent2Sha1(parent2Sha1);
            commitNodeController.setBranchesHistoryScreenController(branchesHistoryScreenController);
            commitNodeController.setSha1(sha1);
            if(activeBranch != null) {
                commitNodeController.setActiveBranch(activeBranch.getBranchName());
            }
            return root;
        } catch (IOException e) {
            return new Label("Error when tried to create graphic node !");
        }
    }

    public Date getDate(){
        return timestamp;
    }
    @Override
    public DoubleBinding getXAnchor(Graph graph, IEdge edge) {
        Region graphic = graph.getGraphic(this);
        System.out.println("added" + edge.getSource() + " -> " + edge.getTarget());
        return graphic.layoutXProperty().add(commitNodeController.getCircleRadius());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommitNode that = (CommitNode) o;

        return Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return timestamp != null ? timestamp.hashCode() : 0;
    }
}
