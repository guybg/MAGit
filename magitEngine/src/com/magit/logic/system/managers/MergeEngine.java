package com.magit.logic.system.managers;

import com.magit.logic.enums.FileStatus;
import com.magit.logic.exceptions.PreviousCommitsLimitExceededException;
import com.magit.logic.system.objects.*;
import com.magit.logic.utils.compare.Delta;
import com.magit.logic.utils.file.WorkingCopyUtils;
import javafx.util.Pair;
import puk.team.course.magit.ancestor.finder.AncestorFinder;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class MergeEngine {


    public void merge(Repository repository, Branch branch) throws ParseException, PreviousCommitsLimitExceededException, IOException {
        String headSha1 = branch.getPointedCommitSha1().toString();

        String sha1OfAncestor = findAncestor(headSha1, "100c91f0c744d2fb5f7c80071c19dc569ce03fce", repository);
        if (sha1OfAncestor.equals(""))
            return;

        String pathToObjectsFolder = repository.getObjectsFolderPath().toString();
        Commit oursCommit = Commit.createCommitInstanceByPath(Paths.get(pathToObjectsFolder, headSha1));
        Commit ancestorCommit = Commit.createCommitInstanceByPath(Paths.get(pathToObjectsFolder, sha1OfAncestor));
        Commit theirsCommit = Commit.createCommitInstanceByPath(Paths.get(pathToObjectsFolder, "100c91f0c744d2fb5f7c80071c19dc569ce03fce"));
        if (null == oursCommit || null == ancestorCommit || null == theirsCommit)
            return;

        String repositoryPath = repository.getRepositoryPath().toString();

        SortedSet<Delta.DeltaFileItem> oursDelta = WorkingCopyUtils.getDeltaFileItemSetFromCommit(oursCommit, repositoryPath);
        SortedSet<Delta.DeltaFileItem> theirsDelta = WorkingCopyUtils.getDeltaFileItemSetFromCommit(theirsCommit, repositoryPath);
        SortedSet<Delta.DeltaFileItem> ancestorDelta = WorkingCopyUtils.getDeltaFileItemSetFromCommit(ancestorCommit, repositoryPath);
        //check fast forward merge
        getMergeState(oursDelta, theirsDelta, ancestorDelta);

    }

    private SortedSet<Pair<String, MergeStateFileItem>> getMergeState(SortedSet<Delta.DeltaFileItem> oursDelta, SortedSet<Delta.DeltaFileItem> theirsDelta,
                                                                      SortedSet<Delta.DeltaFileItem> ancestorDelta) {
        SortedSet<Pair<String, MergeStateFileItem>> mergeStates = new TreeSet<>(Comparator.comparing(Pair::getKey));
        SortedSet<String> pathsOfFiles = new TreeSet<>();
        pathsOfFiles.addAll(setPaths(oursDelta));
        pathsOfFiles.addAll(setPaths(theirsDelta));
        pathsOfFiles.addAll((setPaths(ancestorDelta)));
        Map<String, Pair<FileStatus, Delta.DeltaFileItem>> oursTheirs = Delta.getDiffrencesByPath(oursDelta, theirsDelta);
        Map<String, Pair<FileStatus, Delta.DeltaFileItem>>  theirsAncestor = Delta.getDiffrencesByPath(theirsDelta, ancestorDelta);
        Map<String, Pair<FileStatus, Delta.DeltaFileItem>>  ancestorOurs = Delta.getDiffrencesByPath(ancestorDelta, oursDelta);

        for (String path : pathsOfFiles) {
            FileItem ours, theirs, ancestor;
            Delta.DeltaFileItem temp = oursDelta.stream().filter(i -> i.getFullPath().equals(path)).findFirst().orElse(null);
            ours = null == temp ? null : temp.getFileItem();
            temp = theirsDelta.stream().filter(i -> i.getFullPath().equals(path)).findFirst().orElse(null);
            theirs = null == temp ? null : temp.getFileItem();
            temp = ancestorDelta.stream().filter(i -> i.getFullPath().equals(path)).findFirst().orElse(null);
            ancestor = null == temp ? null : temp.getFileItem();
            FileStatus oursTheirsStatus = oursTheirs.containsKey(path) ? oursTheirs.get(path).getKey() : FileStatus.UNCHANGED;
            FileStatus theirsAncestorStatus = theirsAncestor.containsKey(path) ? theirsAncestor.get(path).getKey() : FileStatus.UNCHANGED;
            FileStatus ancestorOursStatus = ancestorOurs.containsKey(path) ? ancestorOurs.get(path).getKey() : FileStatus.UNCHANGED;
            MergeStateFileItem currentItem = new MergeStateFileItem(ours, theirs, ancestor, oursTheirsStatus, theirsAncestorStatus, ancestorOursStatus);
            mergeStates.add(new Pair<>(path, currentItem));
        }

        return mergeStates;
    }

    private TreeSet<String> setPaths(SortedSet<Delta.DeltaFileItem> delta) {
        TreeSet<String> treeSet = new TreeSet<>();
        for (Delta.DeltaFileItem item : delta) {
            treeSet.add(item.getFullPath());
        }
        return treeSet;
    }

    private String findAncestor(String headSha1, String sha1OfBranchToMerge, Repository repository) {
        AncestorFinder ancestorFinder = new AncestorFinder(sha1 -> {
            String pathToObjectFolder = repository.getObjectsFolderPath().toString();
            Path pathToCommit = Paths.get(pathToObjectFolder, sha1);
            try {
                return Commit.createCommitInstanceByPath(pathToCommit);
            } catch (IOException | ParseException | PreviousCommitsLimitExceededException e) {
                e.printStackTrace();
            }
            return null;
        });

        return ancestorFinder.traceAncestor(headSha1, sha1OfBranchToMerge);
    }
}
