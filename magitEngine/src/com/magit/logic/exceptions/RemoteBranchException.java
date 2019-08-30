package com.magit.logic.exceptions;

public class RemoteBranchException extends Exception{
    private final String mMessage;
    private final String branchName;
    public RemoteBranchException(String mMessage, String branchName) {
        this.mMessage = mMessage;
        this.branchName = branchName;
    }

    @Override
    public String getMessage() {
        return mMessage;
    }

    public String getBranchName() {
        return branchName;
    }
}
