package com.magit.logic.system.tasks;

import com.magit.logic.exceptions.*;
import com.magit.logic.system.MagitEngine;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.IOException;
import java.text.ParseException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NewCommitTask extends Task<Void> {
    MagitEngine engine;
    Runnable onSuccess;

    String input;
    public NewCommitTask(Runnable onSuccess, MagitEngine engine, String input) {
        this.engine = engine;
        this.onSuccess = onSuccess;
        this.input = input;
    }

    @Override
    protected Void call() throws WorkingCopyStatusNotChangedComparedToLastCommitException, ParseException, PreviousCommitsLimitExceededException, IOException, RepositoryNotFoundException, WorkingCopyIsEmptyException, UnhandledConflictsException {
        engine.commit(input);
        Platform.runLater(() -> onSuccess.run());
        return null;
    }

}
