package com.magit.logic.exceptions;

public class WorkingCopyStatusNotChangedComparedToLastCommitException extends Exception {
    public void WorkingCopyStatusNotChangedComparedToLastCommitException() {

    }

    @Override
    public String toString() {
        return "Couldn't generate a new commit due to the fact the working copy hasn't been changed compared to last commit";
    }
}
