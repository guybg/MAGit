package com.magit.logic.exceptions;

public class UncommitedChangesException extends Exception {
    private final String mMessage;

    public UncommitedChangesException(String mMessage) {
        this.mMessage = mMessage;
    }

    @Override
    public String getMessage() {
        return mMessage;
    }
}
