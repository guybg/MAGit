package com.magit.logic.system.tasks;

import com.magit.logic.exceptions.CommitNotFoundException;
import com.magit.logic.exceptions.PreviousCommitsLimitExceededException;
import com.magit.logic.exceptions.RepositoryNotFoundException;
import com.magit.logic.system.MagitEngine;
import com.magit.logic.system.managers.RepositoryManager;
import com.magit.logic.system.objects.FileItemInfo;
import com.magit.logic.system.objects.Repository;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.io.IOException;
import java.text.ParseException;
import java.util.function.Consumer;

public class CollectFileItemsInfoTask extends Task<ObservableList<FileItemInfo>>  {
    private ObservableList<FileItemInfo> files;
    private Consumer<ObservableList<FileItemInfo>> showHistoryDelegate;
    private RepositoryManager manager;

    public CollectFileItemsInfoTask(Consumer<ObservableList<FileItemInfo>> showHistoryDelegate, RepositoryManager manager) {
        this.showHistoryDelegate = showHistoryDelegate;
        this.manager = manager;
    }

    @Override
    protected ObservableList<FileItemInfo> call() throws PreviousCommitsLimitExceededException, RepositoryNotFoundException, CommitNotFoundException, ParseException, IOException {
        files =  manager.guiPresentCurrentCommitAndHistory();
        Platform.runLater(() -> showHistoryDelegate.accept(files));
        return files;
    }
}
