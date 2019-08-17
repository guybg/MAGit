package com.magit.logic.exceptions;

public class WorkingCopyIsEmptyException extends Exception {
    @Override
    public String toString() {
        return "Working Copy is empty, please add some files before you commit a change";
    }

    @Override
    public String getMessage() {
        return "Working Copy is empty, please add some files before you commit a change";
    }
}
