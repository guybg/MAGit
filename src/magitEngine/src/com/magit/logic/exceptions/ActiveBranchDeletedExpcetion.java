package com.magit.logic.exceptions;

public class ActiveBranchDeletedExpcetion extends Exception {
    private String mError;

    public ActiveBranchDeletedExpcetion(String mError) {
        this.mError = mError;
    }

    @Override
    public String toString() {
        return this.mError;
    }
}
