package com.magit.logic.system.managers;

import com.magit.logic.exceptions.PreviousCommitsLimitexceededException;
import com.magit.logic.system.XMLObjects.*;
import com.magit.logic.system.objects.*;
import com.magit.logic.utils.digest.Sha1;
import com.magit.logic.utils.file.WorkingCopyUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class RepositoryXmlParser {

    public Repository parseXMLToRepository(String xmlPath, BranchManager branchManager, String activeUser)
            throws JAXBException, IOException, ParseException, PreviousCommitsLimitexceededException {
        JAXBContext jaxbContext = JAXBContext.newInstance("com.magit.logic.system.XMLObjects");

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        FileInputStream xmlStream = new FileInputStream(new File(xmlPath));

        StreamSource streamSource = new StreamSource(xmlStream);

        JAXBElement<MagitRepository> repositoryJAXBElement =
                unmarshaller.unmarshal(streamSource, MagitRepository.class);

        streamSource.getInputStream().close();
        Repository repository = createRepositoryFromXML(repositoryJAXBElement, branchManager, activeUser);
        xmlStream.close();
        return repository;
    }

    private Repository createRepositoryFromXML(JAXBElement<MagitRepository> jaxbElement, BranchManager branchManager, String activeUser)
            throws ParseException, IOException, PreviousCommitsLimitexceededException {
        MagitRepository magitRepository = jaxbElement.getValue();
        Repository repository = new Repository(Paths.get(magitRepository.getLocation()).toString(), activeUser, magitRepository.getName());
        HashMap<String, Blob> blobMap = createBlobMap(magitRepository);
        HashMap<String, Tree> treeMap = createFolderMap(magitRepository);
        insertFileItemsToTrees(magitRepository.getMagitFolders(), treeMap, blobMap);
        ArrayList<Commit> commits = createCommitsInstances(magitRepository, treeMap);
        createBranches(magitRepository, repository, branchManager, commits);
        repository.create();
        zipCommitWorkingCopy(repository, commits, treeMap);

        return repository;
    }

    private HashMap<String, Blob> createBlobMap(MagitRepository magitRepository) throws ParseException {
        HashMap<String, Blob> blobMap = new HashMap<>();
        for (MagitBlob magitBlob : magitRepository.getMagitBlobs().getMagitBlob()) {
            Blob blobToAdd = new Blob(magitBlob);
            blobMap.put(magitBlob.getId(), blobToAdd);
        }
        return blobMap;
    }

    private HashMap<String, Tree> createFolderMap(MagitRepository magitRepository) throws ParseException {
        HashMap<String, Tree> folderMap = new HashMap<>();
        for (MagitSingleFolder magitFolder : magitRepository.getMagitFolders().getMagitSingleFolder()) {
            Tree treeToAdd = new Tree(magitFolder);
            folderMap.put(magitFolder.getId(), treeToAdd);
        }
        return folderMap;
    }

    private void insertFileItemsToTrees(MagitFolders magitFolders, HashMap<String, Tree> treeMap, HashMap<String, Blob> blobMap) {
        magitFolders.getMagitSingleFolder().stream().filter(folder -> folder.isIsRoot())
                .forEach(folder -> insertFileItemsToTree(magitFolders, folder, treeMap, blobMap));
    }

    private void insertFileItemsToTree(MagitFolders magitFolders, MagitSingleFolder folder,
                                       HashMap<String, Tree> treeMap, HashMap<String, Blob> blobMap) {
        for (Item item : folder.getItems().getItem()) {
            if (item.getType().equals("blob"))
                treeMap.get(folder.getId()).addFileItem(blobMap.get(item.getId()));
            else if (item.getType().equals("folder")) {
                MagitSingleFolder nextFolderInHirerchy = magitFolders.getMagitSingleFolder().stream()
                        .filter(f -> f.getId().equals(item.getId())).findFirst().get();
                insertFileItemsToTree(magitFolders, nextFolderInHirerchy, treeMap, blobMap);
                treeMap.get(folder.getId()).addFileItem(treeMap.get(item.getId()));
            }
        }
    }


    private ArrayList<Commit> createCommitsInstances(MagitRepository magitRepository, HashMap<String, Tree> folders) throws ParseException, PreviousCommitsLimitexceededException, IOException {
        //check why there's root folder in xml
        HashMap<String, Commit> commitsOfRepository = new HashMap<>();
        for (MagitSingleCommit magitCommit : magitRepository.getMagitCommits().getMagitSingleCommit()) {
            Commit currentCommit = new Commit(magitCommit, folders.get(magitCommit.getRootFolder().getId()).getSha1Code(), magitRepository);
            commitsOfRepository.put(magitCommit.getId(), currentCommit);
        }
        for (MagitSingleCommit magitCommit : magitRepository.getMagitCommits().getMagitSingleCommit()) {
            PrecedingCommits precedingCommits = magitCommit.getPrecedingCommits();
            if (precedingCommits == null) {
                precedingCommits = new PrecedingCommits();
            }
            for (PrecedingCommits.PrecedingCommit precedingCommit : precedingCommits.getPrecedingCommit()) {
                commitsOfRepository.get(magitCommit.getId()).addPreceding(commitsOfRepository.get(precedingCommit.getId()).getSha1());
            }
        }
        ArrayList<Commit> commitsArray = new ArrayList<>(commitsOfRepository.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList()));
        return commitsArray;
    }

    private void createBranches(MagitRepository magitRepository, Repository repository,
                                BranchManager branchManager, ArrayList<Commit> commits) {
        String headBranchName = magitRepository.getMagitBranches().getHead();
        for (MagitSingleBranch branch : magitRepository.getMagitBranches().getMagitSingleBranch()) {

            int indexOfCommit = Integer.parseInt(branch.getPointedCommit().getId()) - 1;
            Sha1 branchContent = new Sha1(commits.get(indexOfCommit).getSha1(), true);

            if (branch.getName().equals(headBranchName)) {
                Branch headBranch = new Branch(branch.getName(), branchContent.toString());
                branchManager.setActiveBranch(headBranch);
                repository.addBranch(headBranch.getmBranchName(), headBranch);
                repository.addBranch("HEAD", headBranch);
            } else {
                Branch branchToAdd = new Branch(branch.getName(), branchContent.toString());
                repository.addBranch(branchToAdd.getmBranchName(), branchToAdd);
            }
        }
    }

    private void zipCommitWorkingCopy(Repository repository, ArrayList<Commit> commits, HashMap<String, Tree> treeMap)
            throws IOException {
        for (Commit commit : commits) {
            commit.generateCommitFile(repository.getObjectsFolderPath());
            Sha1 commitWorkingCopySha1commit = commit.getmWorkingCopySha1();
            for (Tree treeEntry : treeMap.values()) {
                if (treeEntry.getSha1Code().equals(commitWorkingCopySha1commit)) {
                    WorkingCopyUtils workingCopyUtils = new WorkingCopyUtils(repository.getRepositoryPath().toString(),
                            treeEntry.getmLastUpdater(), treeEntry.getLastModified());
                    workingCopyUtils.zipWorkingCopyFromTreeWC(treeEntry);
                }
            }
        }
    }
}
