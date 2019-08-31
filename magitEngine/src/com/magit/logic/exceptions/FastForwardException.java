package com.magit.logic.exceptions;

public class FastForwardException extends Exception{
    private final String mMessage;

    public FastForwardException(String mMessage) {
        this.mMessage = mMessage;
    }

    @Override
    public String getMessage() {
        return mMessage;
    }
}
