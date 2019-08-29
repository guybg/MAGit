package com.magit.logic.system.managers;

import com.fxgraph.edges.Edge;
import com.fxgraph.graph.Model;
import com.magit.controllers.BranchesHistoryScreenController;
import com.magit.logic.enums.FileStatus;
import com.magit.logic.exceptions.*;
import com.magit.logic.system.objects.Branch;
import com.magit.logic.system.objects.Commit;
import com.magit.logic.system.objects.Repository;
import com.magit.logic.utils.compare.Delta;
import com.magit.logic.utils.digest.Sha1;
import com.magit.logic.utils.file.FileHandler;
import com.magit.logic.utils.file.WorkingCopyUtils;
import com.magit.logic.visual.node.CommitNode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;

public class BranchManager {
    private Branch mActiveBranch;

    public Branch getActiveBranch() {
        return mActiveBranch;
    }

    public void setActiveBranch(Branch branch) {
        mActiveBranch = branch;
    }

    public void createNewBranch(String branchName, Repository repository,Boolean isRemote,Boolean isTracking, String trackingAfter ) throws IOException, InvalidNameException, BranchAlreadyExistsException {
        final String BLANK_SPACE = " \t\u00A0\u1680\u180e\u2000\u200a\u202f\u205f\u3000\u2800";
        if (StringUtils.containsAny(branchName, BLANK_SPACE) || branchName.isEmpty()) {
            throw new InvalidNameException("Branch name cannot contain blank spaces, please choose a name without blank space and try again.");
        }
        if (Files.exists(Paths.get(repository.getBranchDirectoryPath().toString(), branchName)))
            throw new BranchAlreadyExistsException(branchName);


        repository.addBranch(branchName, new Branch(branchName, mActiveBranch.getPointedCommitSha1().toString(),trackingAfter,isRemote,isTracking));
        writeBranch(repository,branchName,mActiveBranch.getPointedCommitSha1().toString(),isRemote,isTracking,trackingAfter);
    }

    public static void writeBranch(Repository repository, String branchName,String sha1OfCommit,Boolean isRemote,Boolean isTracking, String trackingAfter ) throws IOException {
        //TODO PROBLEM HERE!
        FileHandler.appendFileWithContentAndLine(Paths.get(repository.getBranchDirectoryPath().toString(), branchName).toString(), sha1OfCommit);
        FileHandler.appendFileWithContentAndLine(Paths.get(repository.getBranchDirectoryPath().toString(), branchName).toString(), isRemote.toString());
        FileHandler.appendFileWithContentAndLine(Paths.get(repository.getBranchDirectoryPath().toString(), branchName).toString(), isTracking.toString());
        FileHandler.appendFileWithContentAndLine(Paths.get(repository.getBranchDirectoryPath().toString(), branchName).toString(), trackingAfter);
    }

    public void createNewBranch(String branchName, Repository repository, String sha1OfCommit,Boolean isRemote,Boolean isTracking, String trackingAfter ) throws IOException, InvalidNameException, BranchAlreadyExistsException {
        final String BLANK_SPACE = " \t\u00A0\u1680\u180e\u2000\u200a\u202f\u205f\u3000\u2800";
        if (StringUtils.containsAny(branchName, BLANK_SPACE) || branchName.isEmpty()) {
            throw new InvalidNameException("Branch name cannot contain blank spaces, please choose a name without blank space and try again.");
        }
        if (Files.exists(Paths.get(repository.getBranchDirectoryPath().toString(), branchName)))
            throw new BranchAlreadyExistsException(branchName);

        repository.addBranch(branchName, new Branch(branchName, sha1OfCommit,trackingAfter,isRemote,isTracking));
        writeBranch(repository,branchName,sha1OfCommit,isRemote,isTracking,trackingAfter);
    }


    public TreeSet<CommitNode> guiPresentBranchesHistory(Repository activeRepository, Model model, BranchesHistoryScreenController branchesHistoryScreenController) throws IOException, ParseException, PreviousCommitsLimitExceededException {
        TreeSet<CommitNode> nodes = new TreeSet<>();
        ArrayList<Edge> edges = new ArrayList<>();
        for(Map.Entry<String,Branch> branchEntry :  activeRepository.getBranches().entrySet()){
            if(branchEntry.getKey().equals("HEAD")) continue;
            Path pathToBranchFile = Paths.get(activeRepository.getBranchDirectoryPath().toString(),
                    branchEntry.getValue().getBranchName());
            if (Files.notExists(pathToBranchFile))
                throw new FileNotFoundException("No Branch file, repository is invalid...");

            String sha1OfCommit = Repository.readBranchContent(pathToBranchFile.toFile()).get("sha1");
            Path pathToCommit = Paths.get(activeRepository.getObjectsFolderPath().toString(), sha1OfCommit);
            if (Files.notExists(pathToCommit))
                throw new FileNotFoundException("No commit file, there is no history to show...");

            Commit mostRecentCommit = Commit.createCommitInstanceByPath(pathToCommit);
            assert mostRecentCommit != null; // checking if file exists first, if not, throws FileNotFoundException.
            guiBranchesHistory(mostRecentCommit, activeRepository,nodes, edges, model, branchEntry.getValue(), branchesHistoryScreenController);
        }
        return nodes;
    }
    public String presentCurrentBranch(Repository activeRepository) throws IOException, ParseException, PreviousCommitsLimitExceededException {
        Path pathToBranchFile = Paths.get(activeRepository.getBranchDirectoryPath().toString(),
                mActiveBranch.getBranchName());
        if (Files.notExists(pathToBranchFile))
            throw new FileNotFoundException("No Branch file, repository is invalid.");

        String sha1OfCommit = Repository.readBranchContent(pathToBranchFile.toFile()).get("sha1");
        Path pathToCommit = Paths.get(activeRepository.getObjectsFolderPath().toString(), sha1OfCommit);
        if (Files.notExists(pathToCommit))
            throw new FileNotFoundException("No commit file, there is no history to show.");
        final String separator = "===================================================";
        StringBuilder activeBranchHistory = new StringBuilder();
        activeBranchHistory.append(String.format("Branch Name: %s%s%s%s%s%s"
                , mActiveBranch.getBranchName(), System.lineSeparator(), separator, System.lineSeparator(),
                "Current Commit:", System.lineSeparator()));
        Commit mostRecentCommit = Commit.createCommitInstanceByPath(pathToCommit);
        assert mostRecentCommit != null; // checking if file exists first, if not, throws FileNotFoundException.
        activeBranchHistory.append(mostRecentCommit.toPrintFormat());
        getAllPreviousCommitsHistoryString(mostRecentCommit, activeRepository, activeBranchHistory);

        return activeBranchHistory.toString();
    }

    private void getAllPreviousCommitsHistoryString(Commit mostRecentCommit, Repository activeRepository, StringBuilder activeBranchHistoryStringBuilder) throws IOException, ParseException, PreviousCommitsLimitExceededException {
        final String separator = "===================================================";
        if (mostRecentCommit.getSha1Code().toString().equals("")) return;
        for (Sha1 currentSha1 : mostRecentCommit.getLastCommitsSha1Codes()) {
            if (currentSha1.toString().equals("")) return;
            Path currentCommitPath = Paths.get(activeRepository.getObjectsFolderPath().toString(), currentSha1.toString());
            if (Files.notExists(currentCommitPath)) {
                throw new FileNotFoundException("Commit history is invalid, repository invalid.");
            }
            activeBranchHistoryStringBuilder.append(String.format("%s%s", separator, System.lineSeparator()));
            Commit currentCommitInHistory = Commit.createCommitInstanceByPath(currentCommitPath);
            if (currentCommitInHistory == null) return;
            activeBranchHistoryStringBuilder.append(currentCommitInHistory.toPrintFormat());
            getAllPreviousCommitsHistoryString(currentCommitInHistory, activeRepository, activeBranchHistoryStringBuilder);
        }
    }

    public TreeSet<CommitNode> guiBranchesHistory(Commit mostRecentCommit, Repository activeRepository,TreeSet<CommitNode> nodes, ArrayList<Edge> edges, Model model, Branch branch, BranchesHistoryScreenController branchesHistoryScreenController) throws ParseException, PreviousCommitsLimitExceededException, IOException {
        CommitNode child = new CommitNode(mostRecentCommit, branchesHistoryScreenController);
        child.addActiveBranch(branch);
        if(!nodes.contains(child))
            nodes.add(child);
        else{
            for(CommitNode node : nodes){
                if(child.equals(node)){
                    child = node;
                    child.addActiveBranch(branch);
                }
            }
        }
        guiGetAllPreviousCommitsHistoryString(mostRecentCommit,activeRepository,child,nodes,edges, model,branch, branchesHistoryScreenController);
        return nodes;
    }

    private void guiGetAllPreviousCommitsHistoryString(Commit mostRecentCommit, Repository activeRepository,CommitNode currentCommitNode, TreeSet<CommitNode> commits,ArrayList<Edge> edges, Model model, Branch branch, BranchesHistoryScreenController branchesHistoryScreenController) throws IOException, ParseException, PreviousCommitsLimitExceededException {
        if (mostRecentCommit.getSha1Code().toString().equals("")) return;
        for (Sha1 currentSha1 : mostRecentCommit.getLastCommitsSha1Codes()) {
            if (currentSha1.toString().equals("")) return;
            Path currentCommitPath = Paths.get(activeRepository.getObjectsFolderPath().toString(), currentSha1.toString());
            if (Files.notExists(currentCommitPath)) {
                throw new FileNotFoundException("Commit history is invalid, repository invalid...");
            }
            Commit currentCommitInHistory = Commit.createCommitInstanceByPath(currentCommitPath);
            if (currentCommitInHistory == null) return;

            CommitNode parent = new CommitNode(currentCommitInHistory, branchesHistoryScreenController);
            commits.add(parent);
            if(!commits.contains(parent)) {
                guiEdgeExists(currentCommitNode, edges, model, parent);
                parent.addBranch(branch);
                currentCommitNode.addParent(parent);
            }else{
                for(CommitNode commitNode: commits){
                    if(commitNode.equals(parent)){
                        guiEdgeExists(currentCommitNode, edges, model, commitNode);
                        parent = commitNode;
                        currentCommitNode.addParent(commitNode);
                    }
                }
            }
            parent.addBranch(branch);
            guiGetAllPreviousCommitsHistoryString(currentCommitInHistory, activeRepository, parent, commits,edges,model, branch,branchesHistoryScreenController);
        }
    }

    private Boolean guiEdgeExists(CommitNode currentCommitNode, ArrayList<Edge> edges, Model model, CommitNode commitNode) {
        boolean edgeExists = false;
        for(Edge edge1 : edges){
            if(edge1.getSource().equals(currentCommitNode) && edge1.getTarget().equals(commitNode)){
                edgeExists = true;
            }
        }
        if(!edgeExists){
            Edge edge= new Edge(currentCommitNode,commitNode);
            model.addEdge(edge);
            edges.add(edge);
        }
        return edgeExists;
    }


    public void deleteBranch(String branchNameToDelete, Repository activeRepository) throws IOException, ActiveBranchDeletedException, BranchNotFoundException {
        if (Files.notExists(activeRepository.getHeadPath()))
            throw new FileNotFoundException("Head file not found, repository is invalid.");

        String headContent = FileHandler.readFile(activeRepository.getHeadPath().toString());
        if (branchNameToDelete.equals(headContent))
            throw new ActiveBranchDeletedException("Active Branch can't be deleted.");

        if (!activeRepository.getBranches().containsKey(branchNameToDelete))
            throw new BranchNotFoundException(branchNameToDelete, "Branch '" + branchNameToDelete + "' cannot be deleted, because it does not exist at current repository.");

        FileUtils.deleteQuietly(Paths.get(activeRepository.getBranchDirectoryPath().toString(), branchNameToDelete).toFile());
        activeRepository.getBranches().remove(branchNameToDelete);
    }

    public String pickHeadBranch(String wantedBranchName, Repository activeRepository,
                                 Map<FileStatus, SortedSet<Delta.DeltaFileItem>> changes) throws IOException, ParseException, BranchNotFoundException, UncommitedChangesException, PreviousCommitsLimitExceededException, RemoteBranchException {
        if (Files.notExists(Paths.get(activeRepository.getBranchDirectoryPath().toString(), wantedBranchName)))
            throw new BranchNotFoundException(wantedBranchName);

        String headFileContent = FileHandler.readFile(activeRepository.getHeadPath().toString());
        if (headFileContent.equals(wantedBranchName))
            return "Wanted branch is already active.";
        if (activeRepository.headBranchHasUnhandledMerge())
            throw new UncommitedChangesException("There is unsolved merge at that branch, are you sure you want to switch branch without solving the merge?");
        if (activeRepository.areThereChanges(changes))
            throw new UncommitedChangesException("There are unsaved changes, are you sure you want to change branch without generating a commit?");
        if(activeRepository.getBranches().get(wantedBranchName).getIsRemote()){
            throw new RemoteBranchException("You are trying to checkout into a remote branch, this operation is forbidden, would you like to create a remote tracking branch instead?");
        }


        return forcedChangeBranch(wantedBranchName, activeRepository);
    }

    public String forcedChangeBranch(String wantedBranchName, Repository activeRepository) throws IOException, ParseException, PreviousCommitsLimitExceededException {
        FileHandler.writeNewFile(activeRepository.getHeadPath().toString(), wantedBranchName);
        String wantedBranchSha1 = Repository.readBranchContent(
                Paths.get(activeRepository.getBranchDirectoryPath().toString(), wantedBranchName).toFile()).get("sha1");
        Commit branchLatestCommit = Commit.createCommitInstanceByPath(
                Paths.get(activeRepository.getObjectsFolderPath().toString(), wantedBranchSha1));
        FileHandler.clearFolder(activeRepository.getRepositoryPath());
        if (activeRepository.headBranchHasUnhandledMerge()){
            FileUtils.deleteDirectory(Paths.get(activeRepository.getMagitFolderPath().toString(),".merge",getActiveBranch().getBranchName()).toFile());
        }

        if (branchLatestCommit != null) {
            WorkingCopyUtils.unzipWorkingCopyFromCommit(branchLatestCommit,
                    activeRepository.getRepositoryPath().toString(),
                    activeRepository.getRepositoryPath().toString());
        }
        mActiveBranch = activeRepository.getBranches().get(wantedBranchName);
        activeRepository.getBranches().replace("HEAD", mActiveBranch);
        return "Active branch has changed successfully.";
    }

    public void changeBranchPointedCommit(Repository repository, Sha1 commitSha1) throws CommitNotFoundException, IOException {
        boolean commitExists;
        try {
            commitExists = FileHandler.isContentExistsInFile(Paths.get(repository.getMagitFolderPath().toString(), "COMMITS").toString(), commitSha1.toString());
        } catch (IOException e) {
            throw new CommitNotFoundException("There are no commits to go back to.");
        }
        if (!commitExists) {
            throw new CommitNotFoundException("Wrong commit sha1 code, please enter existing commit sha1 code.");
        }

        repository.changeBranchPointer(mActiveBranch, commitSha1);
    }
}
