package com.magit.logic.exceptions;

public class RemoteReferenceException extends Exception{
    private final String mMessage;

    public RemoteReferenceException(String mMessage) {
        this.mMessage = mMessage;
    }

    @Override
    public String getMessage() {
        return mMessage;
    }
}
