package com.magit.logic.exceptions;

public class IllegalPathException extends Exception {
    private final String message;

    public IllegalPathException(String message) {
        super(message);
        this.message = message;
    }


    @Override
    public String getMessage() {
        return message;
    }
}