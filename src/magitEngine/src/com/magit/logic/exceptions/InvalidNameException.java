package com.magit.logic.exceptions;

public class InvalidNameException extends Exception {
    private String mMessage;

    public InvalidNameException(String message) {
        super(message);
        mMessage = message;
    }

    @Override
    public String getMessage() {
        return mMessage;
    }
}
