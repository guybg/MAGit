package com.magit.logic.system;

import com.magit.logic.handle.exceptions.IllegalPathException;
import com.magit.logic.handle.exceptions.RepositoryAlreadyExistsException;

import java.io.IOException;

public class main {
    public static void main(String[] args) {
        MAGitSystem maGitSystem= new MAGitSystem();
        try {
            maGitSystem.createNewRepository("testRep2", "c:/testingRep");

        } catch (RepositoryAlreadyExistsException e) {
            System.out.println(e.getMessage() + "\n" +
            e.getCause());

        } catch (IllegalPathException e) {
            System.out.println(e.getMessage());
        }catch (IOException e){

        }

    }
}
