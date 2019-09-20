package com.magit.logic.exceptions;

public class ResolveMergeStatusException extends Exception {
    private final String mMessage;

    public ResolveMergeStatusException(String mMessage) {
        this.mMessage = mMessage;
    }

    @Override
    public String getMessage() {
        return mMessage;
    }
}
