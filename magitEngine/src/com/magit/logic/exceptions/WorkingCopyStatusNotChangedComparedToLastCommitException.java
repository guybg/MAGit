package com.magit.logic.exceptions;

public class WorkingCopyStatusNotChangedComparedToLastCommitException extends Exception {
    @Override
    public String toString() {
        return getMessage();
    }

    @Override
    public String getMessage() {
        return "Couldn't generate a new commit due to the fact the working copy hasn't been changed compared to last commit";
    }
}
