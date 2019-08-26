package com.magit.logic.exceptions;

public class UnhandledConflictsException extends Exception{
    private final String mMessage;

    public UnhandledConflictsException(String mMessage) {
        this.mMessage = mMessage;
    }

    @Override
    public String getMessage() {
        return mMessage;
    }
}
