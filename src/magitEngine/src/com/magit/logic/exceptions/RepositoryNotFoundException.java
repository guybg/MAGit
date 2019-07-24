package com.magit.logic.exceptions;


public class RepositoryNotFoundException extends Exception {
    private String repositoryName;

    public RepositoryNotFoundException(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    @Override
    public String toString() {
        return "Repository: " + this.repositoryName + ", not found, Please enter the correct path";
    }
}
