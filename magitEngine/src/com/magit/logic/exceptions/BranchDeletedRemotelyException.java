package com.magit.logic.exceptions;

public class BranchDeletedRemotelyException extends Exception {
    private final String mBranchName;
    private final String mMessage;

    public BranchDeletedRemotelyException(String branchName) {
        this.mBranchName = branchName;
        this.mMessage = "Branch: '" + mBranchName + "' does not exist.";
    }

    public BranchDeletedRemotelyException(String branchName, String message) {
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

    public String getmBranchName() {
        return mBranchName;
    }
}
