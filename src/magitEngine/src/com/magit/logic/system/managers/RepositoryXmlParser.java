package com.magit.logic.system.managers;

import com.magit.logic.system.XMLObjects.*;
import com.magit.logic.system.objects.*;
import com.magit.logic.utils.digest.Sha1;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

public class RepositoryXmlParser {

    public static Repository parseXMLToRepository(String xmlPath, BranchManager branchManager, String activeUser)
     throws JAXBException, FileNotFoundException, ParseException {
        JAXBContext jaxbContext = JAXBContext.newInstance("com.magit.logic.system.XMLObjects");

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        FileInputStream xmlStream = new FileInputStream(new File(xmlPath));

        JAXBElement<MagitRepository> repositoryJAXBElement =
                unmarshaller.unmarshal(new StreamSource(xmlStream), MagitRepository.class);

        return createRepositoryFromXML(repositoryJAXBElement, branchManager, activeUser);
    }

    private static Repository createRepositoryFromXML(JAXBElement<MagitRepository> jaxbElement, BranchManager branchManager, String activeUser)
            throws ParseException {
        MagitRepository magitRepository = jaxbElement.getValue();

        Repository repository = new Repository(magitRepository.getName(), magitRepository.getLocation(), activeUser);
        HashMap<String, Blob> blobMap = createBlobMap(magitRepository);
        HashMap<String, Tree> folderMap = createFolderMap(magitRepository);
        insertFileItemsToTrees(magitRepository, folderMap, blobMap);
        ArrayList<Commit> commitsArray = createCommitsInstance(magitRepository);
        createBranches(magitRepository ,repository, branchManager, commitsArray);

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
                String itemId = itemToAddToTree.getType();
                if (itemId.equals("blob"))
                    folderMap.get(magitFolderId).addFileItem(blobMap.get(itemId));
                else if (itemId.equals("folder"))
                    folderMap.get(magitFolderId).addFileItem(folderMap.get(itemId));
            }
        }
    }

    private static ArrayList<Commit> createCommitsInstance(MagitRepository magitRepository) throws ParseException {
        //check why there's root folder in xml
        ArrayList<Commit> commitsOfRepository = new ArrayList<>();
        for (MagitSingleCommit magitCommit : magitRepository.getMagitCommits().getMagitSingleCommit()) {
            Commit currectCommit = new Commit(magitCommit);
            commitsOfRepository.add(currectCommit);
            for (PrecedingCommits.PrecedingCommit precedingCommit : magitCommit.getPrecedingCommits().getPrecedingCommit()) {
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
            }
            else {
                Branch branchToAdd = new Branch(branch.getName(),
                        new Sha1(commits.get(indexOfCommit).toString(), true).toString());
                repository.addBranch(branchToAdd.getmBranchName(), branchToAdd);
            }
        }
    }
}
