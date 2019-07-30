package com.magit.logic.exceptions;

public class UncommitedChangesException extends Exception {
    String mMessage;

    public UncommitedChangesException(String mMessage) {
        this.mMessage = mMessage;
    }

    @Override
    public String getMessage() {
        return mMessage;
    }
}
