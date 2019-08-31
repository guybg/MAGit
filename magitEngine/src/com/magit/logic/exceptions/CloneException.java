package com.magit.logic.exceptions;

public class CloneException extends Exception{
    private final String mMessage;

    public CloneException(String mMessage) {
        this.mMessage = mMessage;
    }

    @Override
    public String getMessage() {
        return mMessage;
    }
}
