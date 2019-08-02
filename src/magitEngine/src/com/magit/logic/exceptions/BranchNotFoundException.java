package com.magit.logic.exceptions;

public class BranchNotFoundException extends Exception {
    private String mBranchName;
    private String mMessage;

    public BranchNotFoundException(String branchName) {
        this.mBranchName = branchName;
        this.mMessage = "Branch: '" + mBranchName + "' does not exist.";
    }

    public BranchNotFoundException(String branchName, String message) {
        this.mBranchName = branchName;
        this.mMessage = message;
    }

    @Override
    public String toString() {
        return getMessage();
    }

    @Override
    public String getMessage() {
        return mMessage;
    }
}
