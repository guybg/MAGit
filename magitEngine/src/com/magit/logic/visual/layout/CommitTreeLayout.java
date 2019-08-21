package com.magit.logic.visual.layout;

import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.layout.Layout;
import com.magit.logic.visual.node.CommitNode;


import java.util.List;

// simple test for scattering commits in imaginary tree, where every 3rd node is in a new 'branch' (moved to the right)
public class CommitTreeLayout implements Layout {
    @Override
    public void execute(Graph graph) {
        final List<ICell> cells = graph.getModel().getAllCells();
        int startX = 30;
        int startY = 50;
        for (ICell cell : cells) {
            CommitNode c = (CommitNode) cell;
            if(cell.getCellParents().size() > 0){
                ICell p1 = cell.getCellParents().get(0);
                if (cell.getCellParents().size() > 1){
                    ICell p2 = cell.getCellParents().get(1);
                    graph.getGraphic(c).relocate(Math.min(graph.getGraphic(p1).getLayoutX()+
                                    (p1.getCellChildren().size() -1- (p1.getCellChildren().indexOf(cell)))*startX,
                            graph.getGraphic(p2).getLayoutX() + (p2.getCellChildren().size() -1- (p2.getCellChildren().indexOf(cell)))*startX),startY);
                } else if(cell.getCellParents().size() == 1) {
                    graph.getGraphic(c).relocate(graph.getGraphic(p1).getLayoutX() + (p1.getCellChildren().size() - 1 - (p1.getCellChildren().indexOf(cell))) * startX,
                            startY);
                }
            }else{
                graph.getGraphic(c).relocate(startX, startY);
            }
            startY += 50;
        }
    }
}
