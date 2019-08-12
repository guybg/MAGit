package com.magit.logic.exceptions;

import java.nio.file.FileAlreadyExistsException;

public class RepositoryAlreadyExistsException extends FileAlreadyExistsException {
    private String message;

    public RepositoryAlreadyExistsException(String repositoryFullPath, String repositoryName) {
        super(repositoryFullPath);
        message = "Theres already a repository named " + repositoryName + " at " + repositoryFullPath
                + " ,Please consider using another repository name/location and try again.";
    }

    @Override
    public String getMessage() {
        return message;
    }
}
