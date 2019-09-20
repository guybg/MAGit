package com.magit.logic.exceptions;

public class MergeException extends Exception {
    private final String mMessage;

    public MergeException(String mMessage) {
        this.mMessage = mMessage;
    }

    @Override
    public String getMessage() {
        return mMessage;
    }
}
