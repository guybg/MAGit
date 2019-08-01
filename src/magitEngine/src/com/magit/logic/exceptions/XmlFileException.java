package com.magit.logic.exceptions;

public class XmlFileException extends Exception {
    private String mMessage;

    public XmlFileException(String message) {
        mMessage = message;
    }

    @Override
    public String getMessage() {
        return mMessage;
    }
}
