package com.magit.logic.exceptions;

public class ActiveBranchDeletedException extends Exception {
    private String mError;

    public ActiveBranchDeletedException(String mError) {
        this.mError = mError;
    }

    @Override
    public String toString() {
        return this.mError;
    }

    @Override
    public String getMessage() {
        return mError;
    }
}
