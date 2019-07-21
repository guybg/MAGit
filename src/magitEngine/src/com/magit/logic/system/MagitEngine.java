package com.magit.logic.system;

import com.magit.logic.exceptions.IllegalPathException;
import com.magit.logic.system.objects.Branch;
import com.magit.logic.system.objects.Repository;

import java.io.IOException;

public class MagitEngine {

    public MagitEngine() {
    }
    private Repository mActiveRepository;
    private Branch mActiveBranch;

    public static void main(String[] args) {

    }

    public void createNewRepository(String repositoryName, String fullPath) throws IllegalPathException, IOException {
        Repository repository = new Repository(repositoryName, fullPath);
        repository.create();

    }
}


