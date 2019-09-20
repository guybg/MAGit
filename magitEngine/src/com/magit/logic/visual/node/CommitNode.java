package com.magit.logic.visual.node;

import com.fxgraph.cells.AbstractCell;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.IEdge;
import com.magit.controllers.BranchesHistoryScreenController;
import com.magit.controllers.CommitNodeController;
import com.magit.logic.system.objects.Branch;
import com.magit.logic.system.objects.Commit;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class CommitNode extends AbstractCell implements Comparable<CommitNode>{
    private Date timestamp;
    private String committer;
    private String message;
    private CommitNodeController commitNodeController;
    private String sha1;
    private String parent1Sha1;
    private String parent2Sha1;
    private HashMap<String, CommitNode> parents;
    private BranchesHistoryScreenController branchesHistoryScreenController;
    private Integer actualPosX;
    private Integer posX;
    private Integer posY;

    private HashSet<Branch> activeBranches = new HashSet<>();
    private HashSet<Branch> branches = new HashSet<>();
    private boolean alreadySet = false;
    private SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss:SSS");


    public boolean isAlreadySet() {
        return alreadySet;
    }

    public Label getActiveBranchLabel() {
        return commitNodeController.getActiveBranchesLabel();
    }

    public void setAlreadySet(boolean alreadySet) {
        this.alreadySet = alreadySet;
    }

    public HashSet<Branch> getActiveBranches() {
        return activeBranches;
    }

    public void addActiveBranch(Branch activeBranch) {
        this.activeBranches.add(activeBranch);
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
        parents = new HashMap<>();
        this.timestamp = commit.getCreationDate();
        this.committer = commit.getLastUpdater();
        this.message = commit.getCommitMessage();
        this.sha1 = commit.getSha1();
        this.parent1Sha1 = commit.getFirstPrecedingSha1();
        this.parent2Sha1 = commit.getSecondPrecedingSha1();
        this.branchesHistoryScreenController = branchesHistoryScreenController;
    }

    public Integer getActualPosX() {
        return actualPosX;
    }

    public void setActualPosX(Integer actualPosX) {
        this.actualPosX = actualPosX;
    }

    public Integer getPos() {
        return posX;
    }

    public void setPos(int pos) {
        this.posX = pos;
    }

    public void addParent(CommitNode parent) {
        if(!parents.containsKey(parent.sha1)){
            parents.put(parent.sha1,parent);
        }
    }


    @Override
    public Region getGraphic(Graph graph) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource("/com/magit/resources/fxml/commitNode.fxml");
            fxmlLoader.setLocation(url);
            HBox root = fxmlLoader.load(url.openStream());
            commitNodeController = fxmlLoader.getController();
            commitNodeController.setCommitMessage(message);
            commitNodeController.setCommitter(committer);
            commitNodeController.setCommitTimeStamp(formatter.format(timestamp));
            commitNodeController.setParent1Sha1(parent1Sha1);
            commitNodeController.setParent2Sha1(parent2Sha1);
            commitNodeController.setBranchesHistoryScreenController(branchesHistoryScreenController);
            commitNodeController.setSha1(sha1);
            commitNodeController.setParents(parents);
            commitNodeController.setClickedActiveBranches(branchesHistoryScreenController.getClickedOnActiveBranchesProperty());
            ArrayList<String> allBranches = branches.stream().map(Branch::getBranchName).collect(Collectors.toCollection(ArrayList::new));
            if(activeBranches!=null)
                allBranches.addAll(activeBranches.stream().map(Branch::getBranchName).collect(Collectors.toCollection(ArrayList::new)));
            commitNodeController.setBranches(allBranches);
            if(activeBranches.size() != 0) {
                commitNodeController.setActiveBranch(activeBranches);
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
        //System.out.println("added" + edge.getSource() + " -> " + edge.getTarget());
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

    public void showMe(){
        commitNodeController.onCommitClicked(null);
    }

    public String getSha1() {
        return sha1;
    }
}
