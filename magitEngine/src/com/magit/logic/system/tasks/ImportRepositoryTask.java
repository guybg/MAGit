package com.magit.logic.system.tasks;

import com.magit.logic.exceptions.*;
import com.magit.logic.system.MagitEngine;
import com.magit.logic.system.managers.BranchManager;
import com.magit.logic.system.managers.RepositoryManager;
import com.magit.logic.system.managers.RepositoryXmlParser;
import com.magit.logic.system.objects.Repository;
import javafx.concurrent.Task;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.text.ParseException;
import java.util.function.Supplier;

public class ImportRepositoryTask extends Task<Boolean> {

    private int objectsCount = 0;
    private int currentObjectCount = 0;
    private String filePath = null;
    private BranchManager branchManager;
    private RepositoryManager repositoryManager;
    private boolean forceCreation = false;
    private RepositoryXmlParser xmlParser;
    private MagitEngine engine;

    public ImportRepositoryTask(String filePath, MagitEngine engine, boolean forceCreation) {
        this.filePath = filePath;
        this.branchManager = engine.getmBranchManager();
        this.forceCreation = forceCreation;
        this.repositoryManager = engine.getmRepositoryManager();
        this.engine = engine;
    }


    @Override
    protected Boolean call() {
        if (!initializeXmlParser())
            return false;

        if (!commenceXmlValidityChecks())
            return false;

        if (!handleFoldersChecks())
            return false;

        if (importObject("Importing blobs...", this::importBlobs))
            return false;

        if (importObject("Importing folders...", this::importFolders))
            return false;

        xmlParser.buildTree();
        if (importObject("Importing commits...", this::importCommits))
            return false;
        try {
             //repositoryManager.setActiveRepository(xmlParser.createRepositoryFromXML(branchManager));
            updateMessage("Initializing repository...");
            xmlParser.initializeRepository(branchManager);
            importObject("Importing branches...", this::importBranches);
            updateMessage("Creating repository...");
            repositoryManager.setActiveRepository(xmlParser.createRepository());
        } catch(IOException | IllegalPathException ex) {
            updateMessage(ex.getMessage());
            return false;
        }
        try {
            updateMessage("Unziping files...");
            engine.loadHeadBranchCommitFiles(filePath, true);
            updateProgress(currentObjectCount+2, objectsCount);
        } catch (JAXBException | RepositoryAlreadyExistsException | IllegalPathException | XmlFileException | PreviousCommitsLimitExceededException | ParseException | IOException e) {
            e.printStackTrace();
        }
        updateMessage("Repository created successfully!");
        return true;
    }


    private boolean initializeXmlParser(){
        updateMessage("Fetching file...");
        updateProgress(0, 1);

        try {
            xmlParser = new RepositoryXmlParser(filePath);
        } catch (Exception ex) {
            updateMessage(ex.getMessage());
            return false;
        }
        updateProgress(1, 1);
        return true;
    }

    private boolean commenceXmlValidityChecks() {
        updateMessage("Commencing validity checks...");
        int validityCheckCount = 7;
        updateProgress(0, validityCheckCount);
        try {
            xmlParser.checkXmlValidity();
        } catch (XmlFileException ex) {
            updateMessage(ex.getMessage());
            return false;
        }
        updateProgress(validityCheckCount, validityCheckCount);
        return true;
    }

    private boolean handleFoldersChecks() {
        updateMessage("Checking folder validity...");
        updateProgress(0, 1);
        try {
            xmlParser.handleExistingRepositories(forceCreation);
        } catch (IOException | RepositoryAlreadyExistsException ex) {
            updateMessage(ex.getMessage());
            return false;
        }
        updateProgress(1, 1);
        objectsCount = xmlParser.getObjectsCount();
        return true;
    }

    private boolean importBlobs() {
        try {
            currentObjectCount += xmlParser.importBlobs();
        } catch (ParseException e) {
            updateMessage(e.getMessage());
            return false;
        }
        return true;
    }

    private boolean importFolders() {
        try {
            currentObjectCount += xmlParser.importFolders();
        } catch (ParseException e) {
            updateMessage(e.getMessage());
            return false;
        }
        return true;
    }

    private boolean importCommits() {
        try {
            currentObjectCount += xmlParser.createCommits();
        } catch (ParseException | PreviousCommitsLimitExceededException | IOException e) {
            updateMessage(e.getMessage());
            return false;
        }
        return true;
    }

    private boolean importBranches() {
        currentObjectCount += xmlParser.createBranches(branchManager);
        return true;
    }

    private boolean importObject(String message, Supplier<Boolean> xmlFunc) {
        updateMessage(message);
        boolean output = xmlFunc.get();
        updateProgress(currentObjectCount, objectsCount);
        return !output;
    }
}
