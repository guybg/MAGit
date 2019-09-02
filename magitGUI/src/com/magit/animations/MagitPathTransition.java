package com.magit.animations;

import javafx.animation.PathTransition;
import javafx.scene.Node;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;

public class MagitPathTransition  {

    private int posX;
    private int posY;

    PathTransition pathTransition;

    public MagitPathTransition(int posX, int posY, final Node node) {
        this.posX = posX;
        this.posY = posY;
        Path path = new Path();
        path.getElements().add(new MoveTo(posX, posY));
        path.getElements().add(new CubicCurveTo (50, 50, 50, 50, posX, posY));

        pathTransition = new PathTransition();
        pathTransition.setDuration(Duration.seconds(3));
        pathTransition.setPath(path);
        pathTransition.setCycleCount(PathTransition.INDEFINITE);
        pathTransition.setNode(node);
        pathTransition.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
        pathTransition.setAutoReverse(true);
    }

    public void play() {
        pathTransition.play();
    }



}
