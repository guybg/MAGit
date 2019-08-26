package com.magit.logic.exceptions;

public class MergeNotNeededException extends Exception{
    private final String mMessage;

    public MergeNotNeededException(String mMessage) {
        this.mMessage = mMessage;
    }

    @Override
    public String getMessage() {
        return mMessage;
    }
}
