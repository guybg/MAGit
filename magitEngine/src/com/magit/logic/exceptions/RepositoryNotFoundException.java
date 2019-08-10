package com.magit.logic.exceptions;


public class RepositoryNotFoundException extends Exception {
    private final String mMessage;

    public RepositoryNotFoundException(String message) {
        this.mMessage = message;
    }

    @Override
    public String toString() {
        return "Repository not found, Please enter the correct path";
    }

    @Override
    public String getMessage() {
        return mMessage;
    }
}
