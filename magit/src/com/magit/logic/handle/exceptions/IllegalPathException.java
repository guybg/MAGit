package com.magit.logic.handle.exceptions;

import java.nio.file.InvalidPathException;

public class IllegalPathException extends InvalidPathException {
    private String message;
    private String cause;

   // public IllegalPathException(String path) {
   //     this.message = "The path " + path + " is not a valid path, please write the full path because ";
//
//
   // }
//
    public IllegalPathException(String input, String reason) {
        super(input, reason);
        this.message = "Couldnt create " + input + " as a result of " + getReason();
    }

    // public IllegalPathException(String repositoryFullPath, String repositoryName) {
  //     super(repositoryFullPath);
  //     message = "Theres already a repository named " + repositoryName + " at " + repositoryFullPath
  //             + " ,Please consider using another repository name/location and try again.";
  // }

    @Override
    public String getMessage() {
        return message;
    }
}