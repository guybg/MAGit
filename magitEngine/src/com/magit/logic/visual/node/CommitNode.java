package com.magit.logic.visual.node;

import com.fxgraph.cells.AbstractCell;
import com.fxgraph.edges.Edge;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.graph.IEdge;
import com.magit.controllers.CommitNodeController;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CommitNode extends AbstractCell {

    private String timestamp;
    private String committer;
    private String message;
    private CommitNodeController commitNodeController;

    public CommitNode(String timestamp, String committer, String message) {
        this.timestamp = timestamp;
        this.committer = committer;
        this.message = message;
       // parentsConnection = new ArrayList<>();
    }

  //  public ArrayList<Edge> getParentsConnection() {
  //      return parentsConnection;
  //  }
//
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
            commitNodeController.setCommitTimeStamp(timestamp);

            return root;
        } catch (IOException e) {
            return new Label("Error when tried to create graphic node !");
        }
    }

    //public void addParent(ICell parent){
    //    parentsConnection.add(new Edge(this, parent));
  //  }

    public Date getDate(){
        SimpleDateFormat formatter=new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        try {
            return formatter.parse(timestamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
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

        return timestamp != null ? timestamp.equals(that.timestamp) : that.timestamp == null;
    }

    @Override
    public int hashCode() {
        return timestamp != null ? timestamp.hashCode() : 0;
    }
}
