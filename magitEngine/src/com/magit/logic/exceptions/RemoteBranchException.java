package com.magit.logic.exceptions;

public class RemoteBranchException extends Exception{
    private final String mMessage;

    public RemoteBranchException(String mMessage) {
        this.mMessage = mMessage;
    }

    @Override
    public String getMessage() {
        return mMessage;
    }
}
