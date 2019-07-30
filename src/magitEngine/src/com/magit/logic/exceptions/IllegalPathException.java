package com.magit.logic.exceptions;

import java.nio.file.InvalidPathException;

public class IllegalPathException extends InvalidPathException {
    private String message;

    public IllegalPathException(String input, String reason) {
        super(input, reason);
        this.message = "Couldnt perform on " + input + ", because input " + getReason();
    }


    @Override
    public String getMessage() {
        return message;
    }
}