package com.magit.logic.system.managers;

import com.fxgraph.edges.Edge;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.graph.Model;
import com.magit.logic.enums.FileStatus;
import com.magit.logic.exceptions.*;
import com.magit.logic.system.objects.Branch;
import com.magit.logic.system.objects.Commit;
import com.magit.logic.system.objects.Repository;
import com.magit.logic.utils.compare.Delta;
import com.magit.logic.utils.digest.Sha1;
import com.magit.logic.utils.file.FileHandler;
import com.magit.logic.utils.file.WorkingCopyUtils;
import com.magit.logic.visual.layout.CommitTreeLayout;
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

    void loadBranch(File branchFile) throws IOException {
        String headContent = FileHandler.readFile(branchFile.getPath());
        File headBranch = new File(Paths.get(branchFile.getParent(), headContent).toString());
        mActiveBranch = new Branch(headContent, FileHandler.readFile(headBranch.getPath()));
    }

    public void createNewBranch(String branchName, Repository repository) throws IOException, InvalidNameException, BranchAlreadyExistsException {
        final String BLANK_SPACE = " \t\u00A0\u1680\u180e\u2000\u200a\u202f\u205f\u3000\u2800";
        if (StringUtils.containsAny(branchName, BLANK_SPACE) || branchName.isEmpty()) {
            throw new InvalidNameException("Branch name cannot contain blank spaces, please choose a name without blank space and try again.");
        }
        if (Files.exists(Paths.get(repository.getBranchDirectoryPath().toString(), branchName)))
            throw new BranchAlreadyExistsException(branchName);


        repository.addBranch(branchName, new Branch(branchName, mActiveBranch.getPointedCommitSha1().toString()));
        FileHandler.writeNewFile(Paths.get(repository.getBranchDirectoryPath().toString(), branchName).toString(), mActiveBranch.getPointedCommitSha1().toString());
    }


    public LinkedList<CommitNode> guiPresentCurrentBranch(Repository activeRepository, Model model  ) throws IOException, ParseException, PreviousCommitsLimitExceededException {
        Path pathToBranchFile = Paths.get(activeRepository.getBranchDirectoryPath().toString(),
                mActiveBranch.getBranchName());
        if (Files.notExists(pathToBranchFile))
            throw new FileNotFoundException("No Branch file, repository is invalid...");

        String sha1OfCommit = FileHandler.readFile(pathToBranchFile.toString());
        Path pathToCommit = Paths.get(activeRepository.getObjectsFolderPath().toString(), sha1OfCommit);
        if (Files.notExists(pathToCommit))
            throw new FileNotFoundException("No commit file, there is no history to show...");

        Commit mostRecentCommit = Commit.createCommitInstanceByPath(pathToCommit);
        assert mostRecentCommit != null; // checking if file exists first, if not, throws FileNotFoundException.
        //edges = new ArrayList<>();
        LinkedList<CommitNode> nodes = guiBranchHistory(mostRecentCommit, activeRepository, model);
       // Graph graph = new Graph();
       // Model model = graph.getModel();
       // graph.beginUpdate();
       // for(ICell node : nodes) {
       //     if(!model.getAllCells().contains(node))
       //         model.addCell(node);
       // }
       // for(Edge edge : edges){
       //     model.addEdge(edge);
       // }
//
       // graph.endUpdate();
       // graph.layout(new CommitTreeLayout());
        return nodes;
    }
    public String presentCurrentBranch(Repository activeRepository) throws IOException, ParseException, PreviousCommitsLimitExceededException {
        Path pathToBranchFile = Paths.get(activeRepository.getBranchDirectoryPath().toString(),
                mActiveBranch.getBranchName());
        if (Files.notExists(pathToBranchFile))
            throw new FileNotFoundException("No Branch file, repository is invalid.");

        String sha1OfCommit = FileHandler.readFile(pathToBranchFile.toString());
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

    public LinkedList<CommitNode> guiBranchHistory(Commit mostRecentCommit, Repository activeRepository,  Model model) throws ParseException, PreviousCommitsLimitExceededException, IOException {
        LinkedList<CommitNode> nodes = new LinkedList<>();
        CommitNode child = new CommitNode(mostRecentCommit.getCreationDate().toString(),mostRecentCommit.getLastUpdater(), mostRecentCommit.getCommitMessage());
        nodes.addFirst(child);
        guiGetAllPreviousCommitsHistoryString(mostRecentCommit,activeRepository,child,nodes, model);
        return nodes;
    }

    private void guiGetAllPreviousCommitsHistoryString(Commit mostRecentCommit, Repository activeRepository,CommitNode currentCommitNode, LinkedList<CommitNode> commits, Model model) throws IOException, ParseException, PreviousCommitsLimitExceededException {
        //final String separator = "===================================================";
        if (mostRecentCommit.getSha1Code().toString().equals("")) return;
        for (Sha1 currentSha1 : mostRecentCommit.getLastCommitsSha1Codes()) {
            if (currentSha1.toString().equals("")) return;
            Path currentCommitPath = Paths.get(activeRepository.getObjectsFolderPath().toString(), currentSha1.toString());
            if (Files.notExists(currentCommitPath)) {
                throw new FileNotFoundException("Commit history is invalid, repository invalid...");
            }
            Commit currentCommitInHistory = Commit.createCommitInstanceByPath(currentCommitPath);
            if (currentCommitInHistory == null) return;

            CommitNode parent = new CommitNode(currentCommitInHistory.getCreationDate().toString(),currentCommitInHistory.getLastUpdater(), currentCommitInHistory.getCommitMessage());
           // Edge edge = new Edge((ICell)currentCommitNode,(ICell)parent);

            if(!commits.contains(parent)) {
                commits.addFirst(parent);
                model.addEdge(new Edge(currentCommitNode,parent));
            }else{
                for(CommitNode commitNode: commits){
                    if(commitNode.equals(parent)){
                        model.addEdge(new Edge(currentCommitNode,commitNode));
                    }
                }
            }
            guiGetAllPreviousCommitsHistoryString(currentCommitInHistory, activeRepository, parent, commits,model);
        }
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
                                 Map<FileStatus, SortedSet<Delta.DeltaFileItem>> changes) throws IOException, ParseException, BranchNotFoundException, UncommitedChangesException, PreviousCommitsLimitExceededException {
        if (Files.notExists(Paths.get(activeRepository.getBranchDirectoryPath().toString(), wantedBranchName)))
            throw new BranchNotFoundException(wantedBranchName);

        String headFileContent = FileHandler.readFile(activeRepository.getHeadPath().toString());
        if (headFileContent.equals(wantedBranchName))
            return "Wanted branch is already active.";

        if (activeRepository.areThereChanges(changes))
            throw new UncommitedChangesException("There are unsaved changes, are you sure you want to change branch without generating a commit?");


        return forcedChangeBranch(wantedBranchName, activeRepository);
    }

    public String forcedChangeBranch(String wantedBranchName, Repository activeRepository) throws IOException, ParseException, PreviousCommitsLimitExceededException {
        FileHandler.writeNewFile(activeRepository.getHeadPath().toString(), wantedBranchName);
        String wantedBranchSha1 = FileHandler.readFile(
                Paths.get(activeRepository.getBranchDirectoryPath().toString(), wantedBranchName).toString());
        Commit branchLatestCommit = Commit.createCommitInstanceByPath(
                Paths.get(activeRepository.getObjectsFolderPath().toString(), wantedBranchSha1));
        FileHandler.clearFolder(activeRepository.getRepositoryPath());

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
