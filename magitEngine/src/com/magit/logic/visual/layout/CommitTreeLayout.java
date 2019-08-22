package com.magit.logic.visual.layout;

import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.layout.Layout;
import com.magit.logic.system.objects.Branch;
import com.magit.logic.visual.node.CommitNode;



import java.util.*;

// simple test for scattering commits in imaginary tree, where every 3rd node is in a new 'branch' (moved to the right)
public class CommitTreeLayout implements Layout {
    @Override
    public void execute(Graph graph) {
        final List<ICell> cells = graph.getModel().getAllCells();
        Collections.sort(cells, (o1, o2) -> ((CommitNode) o2).getDate().compareTo(((CommitNode) o1).getDate()));

        int startY = 50;

        for (ICell cell : cells) {
            CommitNode commitNode = (CommitNode) cell;
            if (commitNode.getCellParents().size() == 0) commitNode.setPos(0);
            graph.getGraphic(commitNode).relocate(0, startY);
            commitNode.setPosY(startY);
            startY += 50;
        }
        setPosX(cells, graph);
    }

    private void setPosX(List<ICell> cells, Graph graph) {
        double startX = 30;

        Collections.reverse(cells);
        for (ICell cell : cells) {
            CommitNode commitNode = (CommitNode) cell;
            int posX = commitNode.getPos();
            int curPosX = commitNode.getPos();
            List<ICell> children = commitNode.getCellChildren();
            children.sort((o1, o2) -> ((CommitNode) o2).getDate().compareTo(((CommitNode) o1).getDate()));
            for (ICell node : children) {
                CommitNode curNode = (CommitNode) node;
                HashSet<Branch> unionBranches = new HashSet<>();
                if (!curNode.isAlreadySet()) {
                    unionBranches.addAll(curNode.getBranches());
                    if (curNode.getActiveBranch() != null)
                        unionBranches.add(curNode.getActiveBranch());
                }
                if (unionBranches.containsAll(commitNode.getBranches())) {
                    startX+=5;
                    curNode.setAlreadySet(true);
                    graph.getGraphic(curNode).relocate(posX * startX, curNode.getPosY());
                    fixParentsXPosition(commitNode,graph,startX);
                    graph.getGraphic(commitNode).relocate(posX * startX, commitNode.getPosY());
                    curNode.setPos(posX);
                    curPosX = posX + 1;
                }
            }
            for (ICell node : commitNode.getCellChildren()) {
                CommitNode curNode = (CommitNode) node;
                if (!curNode.isAlreadySet()) {
                    graph.getGraphic(curNode).relocate(curPosX * startX, curNode.getPosY());
                    curNode.setPos(curPosX);
                }
                curPosX++;
            }
        }
    }

    private void fixParentsXPosition(CommitNode commitNode, Graph graph, double startX){
        for(ICell parent : commitNode.getCellParents()){
            CommitNode parentNode = (CommitNode)parent;
            if(parentNode.getPos().equals(commitNode.getPos())){
                graph.getGraphic(parentNode).relocate(parentNode.getPos() * startX, parentNode.getPosY());
            }else{
                return;
            }
            fixParentsXPosition(parentNode, graph, startX);
        }
    }
}
