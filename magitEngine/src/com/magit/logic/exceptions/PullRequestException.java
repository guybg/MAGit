package com.magit.logic.exceptions;

public class PullRequestException extends  Exception{
    private final String mMessage;

    public PullRequestException(String mMessage) {
        this.mMessage = mMessage;

    }

    @Override
    public String getMessage() {
        return mMessage;
    }


}
