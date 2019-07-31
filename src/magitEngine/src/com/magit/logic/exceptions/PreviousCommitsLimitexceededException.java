package com.magit.logic.exceptions;

public class PreviousCommitsLimitexceededException extends Exception {
    private String message;

    public PreviousCommitsLimitexceededException(String message) {
        this.message = message;
    }

    public PreviousCommitsLimitexceededException() {
        message = "Previous commit limit exceeded.";
    }

    @Override
    public String getMessage() {
        return message;
    }
}
