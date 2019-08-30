package com.magit.logic.exceptions;

public class PushException extends  Exception{
    private final String mMessage;

    public PushException(String mMessage) {
        this.mMessage = mMessage;

    }

    @Override
    public String getMessage() {
        return mMessage;
    }


}
