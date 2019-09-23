package com.magit.logic.system.Runnable;



import com.magit.logic.exceptions.*;
import com.magit.logic.system.MagitEngine;
import com.magit.logic.system.managers.BranchManager;
import com.magit.logic.system.managers.RepositoryManager;
import com.magit.logic.system.managers.RepositoryXmlParser;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ImportRepositoryRunnable implements Runnable{

    private int objectsCount = 0;
    private int currentObjectCount = 0;
    private String filePath = null;
    private InputStream xml;
    private BranchManager branchManager;
    private RepositoryManager repositoryManager;
    private boolean forceCreation = false;
    private RepositoryXmlParser xmlParser;
    private MagitEngine engine;
    private Runnable forceCreationRunnable;
    private Consumer<HashMap<String,String>> doAfter;
    private String userNamePath;
    private String serialNumber;
    public ImportRepositoryRunnable(InputStream xml, MagitEngine engine, String userNamePath,String serialNumber, Runnable forceCreationRunnable, Consumer<HashMap<String,String>> doAfter, boolean forceCreation) {
        this.xml = xml;
        this.branchManager = engine.getmBranchManager();
        this.forceCreation = forceCreation;
        this.repositoryManager = engine.getmRepositoryManager();
        this.engine = engine;
        this.forceCreationRunnable = forceCreationRunnable;
        this.doAfter = doAfter;
        this.userNamePath = userNamePath;
        this.serialNumber = serialNumber;
    }

    private boolean importRepositoryXML() throws RepositoryAlreadyExistsException, ParseException, PreviousCommitsLimitExceededException, IOException {
        String repositoryName;
        String commitDate = "No commit";
        String commitMessage = "No commit";
        if (!initializeXmlParser())
            return false;

        if (!commenceXmlValidityChecks())
            return false;

        if (!handleFoldersChecks())
            return false;

        if (importObject(this::importBlobs))
            return false;

        if (importObject(this::importFolders))
            return false;

        xmlParser.buildTree();
        if (importObject(this::importCommits))
            return false;
        try {
            repositoryName = xmlParser.initializeRepository();
            xmlParser.setRemoteReference();
            importObject(this::importBranches);
            repositoryManager.setActiveRepository(xmlParser.createRepository());
        } catch(IOException | IllegalPathException ex) {
            return false;
        }
        try {
            engine.loadHeadBranchCommitFiles();
        } catch (JAXBException | RepositoryAlreadyExistsException | IllegalPathException | XmlFileException | PreviousCommitsLimitExceededException | ParseException | IOException e) {
            e.printStackTrace();
        }
        HashMap<String,String> repositoryDetails = new HashMap<>();
        String numberOfBranches = Integer.toString(engine.getmRepositoryManager().getBranches().size());
        repositoryDetails.put("name",repositoryName);
        repositoryDetails.put("activeBranch",engine.getmRepositoryManager().getHeadBranch());
        repositoryDetails.put("branchesNum", numberOfBranches);
        if(engine.getLastCommitDateAndMessage() != null){
            commitDate = engine.getLastCommitDateAndMessage().get(0);
            commitMessage = engine.getLastCommitDateAndMessage().get(1);
        }
        repositoryDetails.put("commitDate", commitDate);
        repositoryDetails.put("commitMessage", commitMessage);


        doAfter.accept(repositoryDetails);
        return true;
    }

    @Override
    public void run() {
        boolean success = false;
        try {
            success = importRepositoryXML();


        } catch (RepositoryAlreadyExistsException e) {
            forceCreationRunnable.run();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (PreviousCommitsLimitExceededException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private boolean initializeXmlParser(){

        try {
            xmlParser = new RepositoryXmlParser(xml, userNamePath,serialNumber);
        } catch (Exception ex) {

            return false;
        }
        return true;
    }

    private boolean commenceXmlValidityChecks() {
        try {
            xmlParser.checkXmlValidity();
        } catch (XmlFileException ex) {

            return false;
        }
        return true;
    }

    private boolean handleFoldersChecks() throws RepositoryAlreadyExistsException {

        try {
            xmlParser.handleExistingRepositories(forceCreation);
        } catch (IOException ex) {
            return false;
        }
        objectsCount = xmlParser.getObjectsCount();
        return true;
    }

    private boolean importBlobs() {
        try {
            currentObjectCount += xmlParser.importBlobs();
        } catch (ParseException e) {

            return false;
        }
        return true;
    }

    private boolean importFolders() {
        try {
            currentObjectCount += xmlParser.importFolders();
        } catch (ParseException e) {

            return false;
        }
        return true;
    }

    private boolean importCommits() {
        try {
            currentObjectCount += xmlParser.createCommits();
        } catch (ParseException | PreviousCommitsLimitExceededException | IOException e) {

            return false;
        }
        return true;
    }

    private boolean importBranches() {
        currentObjectCount += xmlParser.createBranches(branchManager);
        return true;
    }

    private boolean importObject(Supplier<Boolean> xmlFunc) {

        boolean output = xmlFunc.get();

        return !output;
    }
}
