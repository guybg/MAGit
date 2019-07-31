package com.magit.logic.system.managers;

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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RepositoryXmlParser {

    public static Repository parseXMLToRepository(String xmlPath, BranchManager branchManager, String activeUser)
            throws JAXBException, IOException, ParseException {
        JAXBContext jaxbContext = JAXBContext.newInstance("com.magit.logic.system.XMLObjects");

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        FileInputStream xmlStream = new FileInputStream(new File(xmlPath));

        JAXBElement<MagitRepository> repositoryJAXBElement =
                unmarshaller.unmarshal(new StreamSource(xmlStream), MagitRepository.class);

        return createRepositoryFromXML(repositoryJAXBElement, branchManager, activeUser);
    }

    private static Repository createRepositoryFromXML(JAXBElement<MagitRepository> jaxbElement, BranchManager branchManager, String activeUser)
            throws ParseException, IOException {
        MagitRepository magitRepository = jaxbElement.getValue();

        Repository repository = new Repository(magitRepository.getName(), magitRepository.getLocation(), activeUser);
        HashMap<String, Blob> blobMap = createBlobMap(magitRepository);
        HashMap<String, Tree> folderMap = createFolderMap(magitRepository);
        insertFileItemsToTrees(magitRepository, folderMap, blobMap);
        ArrayList<Commit> commitsArray = createCommitsInstance(magitRepository, folderMap);
        createBranches(magitRepository ,repository, branchManager, commitsArray);
        repository.create();
        for (Commit commit : commitsArray) {
            commit.generateCommitFile(repository.getObjectsFolderPath());
            Sha1 commitWorkingCopySha1commit = commit.getmWorkingCopySha1();
            for (Map.Entry<String, Tree> treeEntry : folderMap.entrySet()) {
                if (treeEntry.getValue().getSha1Code().equals(commitWorkingCopySha1commit)) {
                    WorkingCopyUtils workingCopyUtils = new WorkingCopyUtils(repository.getRepositoryPath().toString(),
                            treeEntry.getValue().getmLastUpdater(), treeEntry.getValue().getLastModified());
                    workingCopyUtils.zipWorkingCopyFromTreeWC(treeEntry.getValue());
                }
            }
        }
        return repository;
    }

    private static HashMap<String, Blob> createBlobMap(MagitRepository magitRepository) throws ParseException {
        HashMap<String, Blob> blobMap = new HashMap<>();
        for (MagitBlob magitBlob : magitRepository.getMagitBlobs().getMagitBlob()) {
            Blob blobToAdd = new Blob(magitBlob);
            blobMap.put(magitBlob.getId(), blobToAdd);
        }
        return blobMap;
    }

    private static HashMap<String, Tree> createFolderMap(MagitRepository magitRepository) throws ParseException {
        HashMap<String, Tree> folderMap = new HashMap<>();
        for (MagitSingleFolder magitFolder : magitRepository.getMagitFolders().getMagitSingleFolder()) {
            Tree treeToAdd = new Tree(magitFolder);
            folderMap.put(magitFolder.getId(), treeToAdd);
        }
        return folderMap;
    }

    private static void insertFileItemsToTrees(MagitRepository magitRepository, HashMap<String, Tree> folderMap,
                                               HashMap<String, Blob> blobMap) {
        for (MagitSingleFolder magitFolder : magitRepository.getMagitFolders().getMagitSingleFolder()) {
            String magitFolderId = magitFolder.getId();
            for (Item itemToAddToTree : magitFolder.getItems().getItem()) {
                String itemType = itemToAddToTree.getType();
                String itemId = itemToAddToTree.getId();
                if (magitFolder.getItems().getItem().stream().map(Item::getId).anyMatch(itemId::equals)) {
                    if (itemType.equals("blob"))
                        folderMap.get(magitFolderId).addFileItem(blobMap.get(itemId));
                    else if (itemType.equals("folder") && magitFolder.getItems().getItem().contains(itemToAddToTree))
                        folderMap.get(magitFolderId).addFileItem(folderMap.get(itemId));
                }
            }
        }
    }

    private static ArrayList<Commit> createCommitsInstance(MagitRepository magitRepository, HashMap<String, Tree> folders) throws ParseException {
        //check why there's root folder in xml
        ArrayList<Commit> commitsOfRepository = new ArrayList<>();
        for (MagitSingleCommit magitCommit : magitRepository.getMagitCommits().getMagitSingleCommit()) {
            Commit currectCommit = new Commit(magitCommit, folders.get(magitCommit.getRootFolder().getId()).getSha1Code());
            commitsOfRepository.add(currectCommit);
            PrecedingCommits precedingCommits = magitCommit.getPrecedingCommits();
            if (precedingCommits == null) {
                precedingCommits = new PrecedingCommits();
            }
            for (PrecedingCommits.PrecedingCommit precedingCommit : precedingCommits.getPrecedingCommit()) {
                int indexInArray = Integer.parseInt(precedingCommit.getId()) - 1;
                currectCommit.addPreceding(commitsOfRepository.get(indexInArray).getSha1());
            }
        }
        return commitsOfRepository;
    }

    private static void createBranches(MagitRepository magitRepository, Repository repository,
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
            }
            else {
                Branch branchToAdd = new Branch(branch.getName(), branchContent.toString());
                repository.addBranch(branchToAdd.getmBranchName(), branchToAdd);
            }
        }
    }
}
