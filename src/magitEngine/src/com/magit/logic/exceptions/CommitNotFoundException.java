package com.magit.logic.exceptions;

public class CommitNotFoundException extends Exception {

    private String mError;

    public CommitNotFoundException(String mError) {
        this.mError = mError;
    }

    @Override
    public String toString() {
        return this.mError;
    }

    @Override
    public String getMessage() {
        return this.mError;
    }
}

