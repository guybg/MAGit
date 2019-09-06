package com.magit.animations;

import com.magit.logic.visual.node.CommitNode;
import javafx.animation.PathTransition;
import javafx.animation.RotateTransition;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;

public class MagitPathTransition  {


    public PathTransition pathTransition;

    public MagitPathTransition(CommitNode destination, final Label node) {
        if (null == node)
            return;

        double x = destination.getActiveBranchLabel().localToScene(destination.getActiveBranchLabel().getBoundsInLocal()).getMinX();
        double y = destination.getActiveBranchLabel().localToScene(destination.getActiveBranchLabel().getBoundsInLocal()).getMinY();
        pathTransition = getTransition(node, x, y);

    }

    public void play() {
        pathTransition.play();
    }

    private static PathTransition getTransition(Label block, double toX, double toY) {

        double fromX = block.getParent().getBoundsInParent().getWidth() / 2;
        double fromY = block.getParent().getBoundsInParent().getHeight() / 2;

        toX -= block.getParent().getParent().getLayoutX() - block.getParent().getBoundsInParent().getWidth() / 2;
        toY -= block.getParent().getParent().getLayoutY() - block.getParent().getBoundsInParent().getHeight() / 2;

        Path path = new Path();
        path.getElements().add(new MoveTo(fromX, fromY));
        path.getElements().add(new LineTo(toX, toY));

        PathTransition transition = new PathTransition();
        transition.setPath(path);
        transition.setNode(block);
        transition.setDuration(Duration.seconds(1));

        return transition;
    }

}


