package com.magit.logic.exceptions;

public class BranchAlreadyExistsException extends Exception {
    private final String message;

    public BranchAlreadyExistsException(String branchName) {
        super(branchName);
        message = "Functioning Branch named '" + branchName + "' already exists at current repository, Please consider using another branch name and try again.";
    }

    @Override
    public String getMessage() {
        return message;
    }
}
