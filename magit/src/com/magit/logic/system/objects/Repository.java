package com.magit.logic.system.objects;

import com.magit.logic.handle.exceptions.IllegalPathException;
import com.magit.logic.handle.exceptions.RepositoryAlreadyExistsException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Repository {
    private String mRepositoryName;
    private String mRepositoryLocation;
    private List<Branch> mBranches;

    public Repository(String mRepositoryName, String mRepositoryLocation) {
        this.mRepositoryName = mRepositoryName;
        this.mRepositoryLocation = mRepositoryLocation;
    }

    public void create() throws RepositoryAlreadyExistsException, IllegalPathException, IOException {
        Boolean validPath = false;
        File repository;
        try {
            Path filePath = Paths.get(mRepositoryLocation,mRepositoryName, ".magit");
            repository = new File(filePath.toString());
            validPath = repository.mkdirs();
        }
        catch (InvalidPathException e){
            System.out.println(e.getMessage());
            throw new IllegalPathException(e.getInput(), e.getMessage());

        }

        if(!validPath) {
            if(repository.exists())
                throw new RepositoryAlreadyExistsException(mRepositoryLocation, mRepositoryName);
            else
                throw new IllegalPathException(mRepositoryLocation, "wrong location");
        }

        Branch branch = new Branch("master");
        branch.create(Paths.get(mRepositoryLocation,mRepositoryName).toString());
        createHeadFile("master");
    }
    private void createHeadFile(String branchName) throws IOException{
            Path path = Paths.get(mRepositoryLocation, mRepositoryName, ".magit", "Branches", "HEAD");
            Files.createFile(path);
            BufferedWriter writer = new BufferedWriter(new FileWriter(path.toString()));
            writer.write(branchName);
            writer.close();
    }
}
