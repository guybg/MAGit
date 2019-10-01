package com.magit.logic.system.managers;

import com.magit.logic.exceptions.*;
import com.magit.logic.system.MagitEngine;
import com.magit.logic.system.objects.*;
import com.magit.logic.utils.file.FileHandler;
import com.magit.logic.utils.file.WorkingCopyUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class CollaborationEngine {
    ArrayList<PullRequest> pullRequests;
    private final String BLANK_SPACE = " \t\u00A0\u1680\u180e\u2000\u200a\u202f\u205f\u3000\u2800";

    public CollaborationEngine() {
        this.pullRequests = new ArrayList<>();
    }

    public ArrayList<PullRequest> getPullRequests() {
        return pullRequests;
    }

    public void addPullRequest(PullRequest pullRequest) {
        this.pullRequests.add(pullRequest);
    }

    public void cloneRepository(String pathToMagitRepository, String destinationPath, String cloneName, BranchManager branchManager) throws IOException, IllegalPathException, CloneException, InvalidNameException {
        if(!isValid(pathToMagitRepository)){
            throw new CloneException("Source repository is invalid");
        }
        if(isValid(destinationPath))
            throw new CloneException("There is already a repository at destination location");
        if(pathToMagitRepository.toLowerCase().equals(destinationPath.toLowerCase()))
            throw new CloneException("Destination location is the same as the source repository location, please choose another destination path");
        if (StringUtils.containsOnly(cloneName, BLANK_SPACE) || cloneName.isEmpty())
            throw new InvalidNameException("Repository name should contain at least one alphanumeric character from A–Z or 0–9 or any symbol that is not a blank space");
        Repository repository = RepositoryManager.loadRepository(Paths.get(pathToMagitRepository), branchManager);

        ClonedRepository clonedRepository = ClonedRepository.getClone(repository,cloneName,destinationPath);

        clonedRepository.create();


    }

    public void fetch(Repository repository) throws RemoteReferenceException, IOException, ParseException, PreviousCommitsLimitExceededException, CommitNotFoundException {
        if(repository.getRemoteReference() == null)
            throw new RemoteReferenceException("Repository does not have remote reference");
        Repository remoteRepository = RepositoryManager.loadRepository(Paths.get(repository.getRemoteReference().getLocation()), new BranchManager());
        for(Branch branch : remoteRepository.getBranches().values()){
            updateRemoteBranch(repository, branch);
        }
        WorkingCopyUtils.updateNewObjects(remoteRepository,repository);
    }

    private String updateRemoteBranch(Repository repository, Branch branch) throws IOException {
        String remoteBranchName = String.join("\\",repository.getRemoteReference().getRepositoryName(),branch.getBranchName());
        if(!repository.getBranches().containsKey(remoteBranchName)){
            Branch remoteBranch = new Branch(
                    remoteBranchName,branch.getPointedCommitSha1().toString(),null, true,false);
            repository.getBranches().put(remoteBranchName,remoteBranch);
            BranchManager.writeBranch(repository,remoteBranchName,remoteBranch.getPointedCommitSha1().toString(),true,false,null);
        }else{
            repository.changeBranchPointer(repository.getBranches().get(remoteBranchName),branch.getPointedCommitSha1());
            BranchManager.writeBranch(repository,remoteBranchName,branch.getPointedCommitSha1().toString(),true,false,null);
        }
        return remoteBranchName;
    }

    public void pull(MagitEngine engine) throws RemoteReferenceException, IOException, ParseException, PreviousCommitsLimitExceededException, CommitNotFoundException, UnhandledMergeException, FastForwardException, MergeNotNeededException, UncommitedChangesException, RepositoryNotFoundException, RemoteBranchException, MergeException {
        engine.activeBranchHasUnhandledMerge();
        engine.workingCopyChangedComparedToCommit();
        Repository repository = engine.getmRepositoryManager().getRepository();
        if(repository.getRemoteReference() == null)
            throw new RemoteReferenceException("Repository does not have remote reference");
        Repository remoteRepository = RepositoryManager.loadRepository(Paths.get(repository.getRemoteReference().getLocation()), new BranchManager());
        if(!repository.getBranches().get("HEAD").getIsTracking())
            throw new RemoteBranchException("Active branch is not tracking any remote branch, cannot pull.",repository.getBranches().get("HEAD").getBranchName());
        Branch activeBranch = remoteRepository.getBranches().get(repository.getBranches().get("HEAD").getBranchName());
        String remoteBranchName = updateRemoteBranch(repository, activeBranch);
        WorkingCopyUtils.updateNewObjectsOfSpecificCommit(remoteRepository,repository, activeBranch.getPointedCommitSha1().toString());

        engine.merge(remoteBranchName, true);
    }

    public void push(MagitEngine engine) throws IOException, RemoteReferenceException, RemoteBranchException, ParseException, PreviousCommitsLimitExceededException, UnhandledMergeException, UncommitedChangesException, PushException, CommitNotFoundException {
        Repository repository = engine.getmRepositoryManager().getRepository();
        if(repository.getRemoteReference() == null)
            throw new RemoteReferenceException("Repository does not have remote reference");
        RepositoryManager remoteRepositoryManager = new RepositoryManager(Paths.get(repository.getRemoteReference().getLocation()),new BranchManager());
        Repository remoteRepository = remoteRepositoryManager.getRepository();
        //if(!repository.getBranches().get("HEAD").getIsTracking())
        //    throw new RemoteBranchException("Active branch is not tracking any remote branch, cannot push.",repository.getBranches().get("HEAD").getBranchName());
        if(remoteRepository.headBranchHasUnhandledMerge()){
            throw new UnhandledMergeException("Cannot push - please solve unhandled merge.");
        }
        if(remoteRepository.areThereChanges(remoteRepositoryManager.checkDifferenceBetweenCurrentWCAndLastCommit())){
            throw new UncommitedChangesException("Cannot push - there are open changes, at remote repository.");
        }
        Branch activeBranchAtLocalRepository = repository.getBranches().get("HEAD");

        if(!activeBranchAtLocalRepository.getIsTracking()){
            Branch remoteRepositoryBranch = new Branch(activeBranchAtLocalRepository.getBranchName(),"",null,false,false);
            remoteRepository.getBranches().put(activeBranchAtLocalRepository.getBranchName(),remoteRepositoryBranch);
            BranchManager.writeBranch(remoteRepository,remoteRepositoryBranch.getBranchName(),"",false,false,null);
            String remoteBranchName = updateRemoteBranch(repository,remoteRepositoryBranch);
            activeBranchAtLocalRepository.setIsTracking(true);
            activeBranchAtLocalRepository.setTrackingAfter(remoteBranchName);
            BranchManager.writeBranch(repository,activeBranchAtLocalRepository.getBranchName(),activeBranchAtLocalRepository.getPointedCommitSha1().toString()
                    ,false,true,remoteBranchName);
        }

        Branch activeBranchsRemoteBranchAtLocalRepository = repository.getBranches().get(activeBranchAtLocalRepository.getTrackingAfter());

        Branch branchAtRemoteRepository = remoteRepository.getBranches().get(activeBranchAtLocalRepository.getBranchName());
        if(!activeBranchsRemoteBranchAtLocalRepository.getPointedCommitSha1().toString().equals(branchAtRemoteRepository.getPointedCommitSha1().toString())){
            throw new PushException("Active branch's remote branch is not synced with Remote repository's matching branch, please pull");
        }
        if(activeBranchAtLocalRepository.getPointedCommitSha1().toString().equals(branchAtRemoteRepository.getPointedCommitSha1().toString())){
            throw new PushException("Remote repository is up-to-date.");
        }

        WorkingCopyUtils.updateNewObjectsOfSpecificCommit(repository,remoteRepository, activeBranchAtLocalRepository.getPointedCommitSha1().toString());
        BranchManager.writeBranch(remoteRepository,branchAtRemoteRepository.getBranchName(),activeBranchAtLocalRepository.getPointedCommitSha1().toString()
        ,branchAtRemoteRepository.getIsRemote(),branchAtRemoteRepository.getIsTracking(),branchAtRemoteRepository.getTrackingAfter());

        if(activeBranchAtLocalRepository.getBranchName().equals(remoteRepositoryManager.getHeadBranch())){
            Commit remoteHeadCommit = Commit.createCommitInstanceByPath(remoteRepository.getCommitPath());
            if(remoteHeadCommit ==null)
                throw new CommitNotFoundException("Commit not found, repository corrupted.");
            FileHandler.clearFolder(Paths.get(remoteRepository.getRepositoryPath().toString()));
            WorkingCopyUtils.unzipWorkingCopyFromCommit(remoteHeadCommit,remoteRepository.getRepositoryPath().toString(),remoteRepository.getRepositoryPath().toString());
        }
        updateRemoteBranch(repository, activeBranchAtLocalRepository);
    }

    public boolean isValid(String repositoryLocation) throws IOException {
        final Path pathToMagit = Paths.get(repositoryLocation,".magit"), pathToHead = Paths.get(repositoryLocation,".magit","branches","HEAD"), repositoryPath = Paths.get(repositoryLocation);
        final String BRANCHES = "BRANCHES";
        return Files.exists(Paths.get(repositoryLocation)) &&
                Files.exists(Paths.get(repositoryLocation)) && Files.exists(pathToMagit) && Files.exists(pathToHead) &&
                !FileHandler.readFile(pathToHead.toString()).isEmpty()
                && Files.exists(Paths.get(pathToMagit.toString(), BRANCHES, FileHandler.readFile(pathToHead.toString())));
    }

    public void createPullRequest(MagitEngine engineOfSender, String targetBranchName,String baseBranchName, String message) throws IOException, UnhandledMergeException, RemoteReferenceException, PushException, RemoteBranchException, CommitNotFoundException, ParseException, UncommitedChangesException, PreviousCommitsLimitExceededException, RepositoryNotFoundException {
        PullRequest requestDetails = new PullRequest(pullRequests.size() ,engineOfSender.getUserName(),targetBranchName,baseBranchName,new Date().toString(),message,PullRequestStatus.Open);
        engineOfSender.push(); // creates branch at receiver, rb, rtb at sender
        //MagitEngine engineOfReceiver = new MagitEngine();
        //engineOfReceiver.switchRepository(Paths.get(engineOfSender.getmRepositoryManager().getRepository().getRemoteReference().getLocation()).toString());
        addPullRequest(requestDetails);
    }

    public void rejectPullRequest(int requestId) throws UnhandledMergeException, MergeNotNeededException, RepositoryNotFoundException, MergeException, UncommitedChangesException, FastForwardException {
        pullRequests.get(requestId).setStatus(PullRequestStatus.Rejected);
    }

    public void acceptPullRequest(MagitEngine engine, int requestId) throws UnhandledMergeException, MergeNotNeededException, RepositoryNotFoundException, MergeException, UncommitedChangesException, InvalidNameException, ParseException, PreviousCommitsLimitExceededException, IOException, BranchNotFoundException, RemoteBranchException, WorkingCopyStatusNotChangedComparedToLastCommitException, UnhandledConflictsException, WorkingCopyIsEmptyException, FastForwardException {
        engine.switchRepository(engine.guiGetRepositoryPath());
        engine.pickHeadBranch(pullRequests.get(requestId).baseBranch);
        try {
            engine.merge(pullRequests.get(requestId).targetBranch, false);
        } catch (FastForwardException e) {
            engine.commit("message");
        }
        pullRequests.get(requestId).setStatus(PullRequestStatus.Closed);
    }

    private enum PullRequestStatus{Open, Closed, Rejected}
    public class PullRequest{
        String requestId;
        String userName;
        String targetBranch;
        String baseBranch;
        String date;
        String message;
        PullRequestStatus status;

        public PullRequest(Integer requestId,String userName, String targetBranch, String baseBranch, String date, String message, PullRequestStatus status) {
            this.requestId = requestId.toString();
            this.userName = userName;
            this.targetBranch = targetBranch;
            this.baseBranch = baseBranch;
            this.date = date;
            this.message = message;
            this.status = status;
        }

        public String getRequestId() {
            return requestId;
        }

        public String getUserName() {
            return userName;
        }

        public String getTargetBranch() {
            return targetBranch;
        }

        public String getBaseBranch() {
            return baseBranch;
        }

        public String getDate() {
            return date;
        }

        public String getMessage() {
            return message;
        }

        public PullRequestStatus getStatus() {
            return status;
        }

        public void setStatus(PullRequestStatus status) {
            this.status = status;
        }
    }
}
