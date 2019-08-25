package com.magit.logic.exceptions;

public class UnhandledMergeException extends Exception {
    private final String mMessage;

    public UnhandledMergeException(String mMessage) {
        this.mMessage = mMessage;
    }

    @Override
    public String getMessage() {
        return mMessage;
    }
}

