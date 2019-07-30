package com.magit.logic.exceptions;


public class RepositoryNotFoundException extends Exception {
    private String repositoryName;
    private String mMessege;

    public RepositoryNotFoundException(String messege) {
        this.repositoryName = repositoryName;
        this.mMessege = messege;
    }

    @Override
    public String toString() {
        return "Repository not found, Please enter the correct path";
    }

    @Override
    public String getMessage() {
        return mMessege;
    }
}
