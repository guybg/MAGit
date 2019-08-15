package com.magit.logic.exceptions;

public class PreviousCommitsLimitExceededException extends Exception {
    private final String message;

    public PreviousCommitsLimitExceededException(String message) {
        this.message = message;
    }

    public PreviousCommitsLimitExceededException() {
        message = "Previous commit limit exceeded.";
    }

    @Override
    public String getMessage() {
        return message;
    }
}
