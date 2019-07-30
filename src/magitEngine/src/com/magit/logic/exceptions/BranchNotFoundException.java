package com.magit.logic.exceptions;

public class BranchNotFoundException extends Exception {
    private String mBranchName;

    public BranchNotFoundException(String branchName) {
        this.mBranchName = branchName;
    }

    @Override
    public String toString() {
        return getMessage();
    }

    @Override
    public String getMessage() {
        return "Branch: " + mBranchName + " doest not exist.";
    }
}
