package com.magit.logic.system.managers;

import com.magit.logic.exceptions.PreviousCommitsLimitexceededException;
import com.magit.logic.exceptions.XmlFileException;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RepositoryXmlParser {

    public Repository parseXMLToRepository(String xmlPath, BranchManager branchManager, String activeUser)
            throws JAXBException, IOException, ParseException, PreviousCommitsLimitexceededException, XmlFileException {
        checkIfXmlFile(xmlPath);
        
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
            throws ParseException, IOException, PreviousCommitsLimitexceededException, XmlFileException {
        MagitRepository magitRepository = jaxbElement.getValue();

        /* check for xml errors */
        checkXmlForDuplicateRepositoryItemIdsError(magitRepository);
        checkXmlFoldersForWrongItemIdPointers(magitRepository);
        checkXmlCommitsForWrongFolderIdPointers(magitRepository);
        checkXmlForInvalidPointedCommitsInBranches(magitRepository);
        checkXmlHeadBranchExists(magitRepository);

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

    private void checkIfXmlFile(String pathToXml) throws XmlFileException {
        if (!Paths.get(pathToXml).getFileName().toString().endsWith(".xml"))
            throw new XmlFileException("Given file is not xml file, file does not and with .xml.");
    }

    private void checkXmlFoldersForWrongItemIdPointers(MagitRepository magitRepository) throws XmlFileException {
        List<MagitBlob> magitBlobs = magitRepository.getMagitBlobs().getMagitBlob();
        List<MagitSingleFolder> magitSingleFolders = magitRepository.getMagitFolders().getMagitSingleFolder();
        for (MagitSingleFolder magitSingleFolder : magitSingleFolders) {
            for (Item item : magitSingleFolder.getItems().getItem()) {
                checkIfItemIdExists(magitRepository, magitSingleFolder, item);
            }
        }
    }

    private void checkXmlCommitsForWrongFolderIdPointers(MagitRepository magitRepository) throws XmlFileException {
        List<MagitSingleFolder> magitSingleFolders = magitRepository.getMagitFolders().getMagitSingleFolder();
        List<MagitSingleCommit> magitSingleCommits = magitRepository.getMagitCommits().getMagitSingleCommit();
        for (MagitSingleCommit magitSingleCommit : magitSingleCommits) {
            checkIfValidCommitFolder(magitRepository, magitSingleCommit, magitSingleCommit.getRootFolder());
        }
    }

    private void checkIfValidCommitFolder(MagitRepository magitRepository, MagitSingleCommit magitSingleCommit, RootFolder rootFolder) throws XmlFileException {
        if (magitRepository.getMagitFolders().getMagitSingleFolder().stream().noneMatch(magitfolder -> magitfolder.getId().equals(rootFolder.getId()) && magitfolder.isIsRoot())) {
            throw new XmlFileException("XML error : Pointed commit folder with id " + rootFolder.getId() + " not found or not defined as a root folder.");
        }
    }

    private void checkIfItemIdExists(MagitRepository magitRepository, MagitSingleFolder magitSingleFolder, Item itemTCheck) throws XmlFileException {
        if (itemTCheck.getType().equals("blob")) {
            if (!magitRepository.getMagitBlobs().getMagitBlob()
                    .stream()
                    .anyMatch(blob -> blob.getId().equals(itemTCheck.getId())))
                throw new XmlFileException("XML error : Folder id " + magitSingleFolder.getId() + " contains the id " + itemTCheck.getId() + " of blob that doesn't exist.");
        } else if (itemTCheck.getType().equals("folder")) {
            if (!magitRepository.getMagitFolders().getMagitSingleFolder()
                    .stream()
                    .anyMatch(folder -> folder.getId().equals(itemTCheck.getId())))
                throw new XmlFileException("XML error : Folder id " + magitSingleFolder.getId() + " contains the id " + itemTCheck.getId() + " of folder that doesn't exist.");
            if (magitSingleFolder.getId().equals(itemTCheck.getId())) {
                throw new XmlFileException("XML error : Folder contains itself.");
            }
        } else {
            throw new XmlFileException("XML error : " + itemTCheck.getType() + " is not a valid item type.");
        }
    }

    private void checkXmlForDuplicateRepositoryItemIdsError(MagitRepository magitRepository) throws XmlFileException {
        List<MagitBlob> magitBlobs = magitRepository.getMagitBlobs().getMagitBlob();
        List<MagitSingleFolder> magitSingleFolders = magitRepository.getMagitFolders().getMagitSingleFolder();
        List<MagitSingleCommit> magitSingleCommits = magitRepository.getMagitCommits().getMagitSingleCommit();
        if (magitBlobs.size() != magitBlobs.stream().map(MagitBlob::getId).distinct().count())
            throw new XmlFileException("XML error : Duplicate Blob ids.");
        if (magitSingleFolders.size() != magitSingleFolders.stream().map(MagitSingleFolder::getId).distinct().count())
            throw new XmlFileException("XML error : Duplicate Folder ids.");
        if (magitSingleCommits.size() != magitSingleCommits.stream().map(MagitSingleCommit::getId).distinct().count())
            throw new XmlFileException("XML error : Duplicate Commit ids.");
    }

    private void checkXmlForInvalidPointedCommitsInBranches(MagitRepository magitRepository) throws XmlFileException {
        List<MagitSingleCommit> magitSingleCommits = magitRepository.getMagitCommits().getMagitSingleCommit();
        List<MagitSingleBranch> magitSingleBranches = magitRepository.getMagitBranches().getMagitSingleBranch();

        for (MagitSingleBranch magitSingleBranch : magitSingleBranches) {
            if (magitSingleCommits.stream().noneMatch(magitCommit -> magitCommit.getId().equals(magitSingleBranch.getPointedCommit().getId()))) {
                throw new XmlFileException("XML error : Branch with the name: '" + magitSingleBranch.getName() + "' has invalid pointed commit id.");
            }
        }
    }

    private void checkXmlHeadBranchExists(MagitRepository magitRepository) throws XmlFileException {
        if (magitRepository.getMagitBranches().getMagitSingleBranch().stream().noneMatch(branch -> branch.getName().equals(magitRepository.getMagitBranches().getHead()))) {
            throw new XmlFileException("Head branch '" + magitRepository.getMagitBranches().getHead() + "' does not exist.");
        }
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
        return new ArrayList<>(commitsOfRepository.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList()));
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
