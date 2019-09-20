package com.magit.logic.system.tasks;

import com.fxgraph.graph.Model;
import com.magit.controllers.BranchesHistoryScreenController;
import com.magit.logic.exceptions.*;
import com.magit.logic.system.MagitEngine;
import com.magit.logic.system.objects.FileItemInfo;
import com.magit.logic.visual.node.CommitNode;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.io.IOException;
import java.text.ParseException;
import java.util.TreeSet;
import java.util.function.Consumer;

public class BranchesHistoryTask extends Task<TreeSet<CommitNode>> {
    MagitEngine engine;
    Consumer<TreeSet<CommitNode>> infoReadyDelegate;
    Model model;
    BranchesHistoryScreenController branchesHistoryScreenController;
    public BranchesHistoryTask(Consumer<TreeSet<CommitNode>> infoReadyDelegate, MagitEngine engine, Model model, BranchesHistoryScreenController branchesHistoryScreenController) {
        this.engine = engine;
        this.infoReadyDelegate = infoReadyDelegate;
        this.model = model;
        this.branchesHistoryScreenController = branchesHistoryScreenController;
    }

    @Override
    protected TreeSet<CommitNode> call() throws  ParseException, PreviousCommitsLimitExceededException, IOException {
        TreeSet<CommitNode> nodes = engine.guiBranchesHistory(model,branchesHistoryScreenController,branchesHistoryScreenController.getToggleStatus());
        Platform.runLater(() -> infoReadyDelegate.accept(nodes));
        return nodes;
    }
}
