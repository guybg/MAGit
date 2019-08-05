package com.magit.logic.exceptions;

import java.nio.file.FileAlreadyExistsException;

public class RepositoryAlreadyExistsException extends FileAlreadyExistsException {
    private final String message;

    public RepositoryAlreadyExistsException(String repositoryFullPath) {
        super(repositoryFullPath);
        message = "Functioning repository already exists at location " + repositoryFullPath
                + " ,Please consider using another location and try again.";
    }

    public RepositoryAlreadyExistsException(String message, String repositoryFullPath) {
        super(repositoryFullPath);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
