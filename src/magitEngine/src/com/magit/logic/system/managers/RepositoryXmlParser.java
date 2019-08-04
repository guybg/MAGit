package com.magit.logic.system.managers;

import com.magit.logic.enums.FileType;
import com.magit.logic.exceptions.IllegalPathException;
import com.magit.logic.exceptions.PreviousCommitsLimitExceededException;
import com.magit.logic.exceptions.RepositoryAlreadyExistsException;
import com.magit.logic.exceptions.XmlFileException;
import com.magit.logic.system.XMLObjects.*;
import com.magit.logic.system.objects.*;
import com.magit.logic.utils.digest.Sha1;
import com.magit.logic.utils.file.FileHandler;
import com.magit.logic.utils.file.WorkingCopyUtils;
import org.apache.commons.io.FileUtils;

import javax.xml.bind.*;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class RepositoryXmlParser {
    private HashMap<String,String> blobSha1ToId;
    private HashMap<String,String> treeSha1ToId;



    public Repository parseXMLToRepository(String xmlPath, BranchManager branchManager, String activeUser, boolean forceCreation)
            throws JAXBException, IOException, ParseException, PreviousCommitsLimitExceededException, XmlFileException, IllegalPathException {
        checkIfXmlFile(xmlPath);

        JAXBContext jaxbContext = JAXBContext.newInstance("com.magit.logic.system.XMLObjects");

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        FileInputStream xmlStream = new FileInputStream(new File(xmlPath));

        StreamSource streamSource = new StreamSource(xmlStream);

        JAXBElement<MagitRepository> repositoryJAXBElement =
                unmarshaller.unmarshal(streamSource, MagitRepository.class);

        streamSource.getInputStream().close();
        Repository repository = createRepositoryFromXML(repositoryJAXBElement, branchManager, activeUser, forceCreation);
        xmlStream.close();
        return repository;
    }

    private Repository createRepositoryFromXML(JAXBElement<MagitRepository> jaxbElement, BranchManager branchManager, String activeUser, boolean forceCreation)
            throws ParseException, IOException, PreviousCommitsLimitExceededException, XmlFileException, IllegalPathException {
        MagitRepository magitRepository = jaxbElement.getValue();

        /* check for xml errors */
        checkXmlForDuplicateRepositoryItemIdsError(magitRepository);
        checkXmlFoldersForWrongItemIdPointers(magitRepository);
        checkXmlCommitsForWrongFolderIdPointers(magitRepository);
        checkXmlForInvalidPointedCommitsInBranches(magitRepository);
        checkXmlHeadBranchExists(magitRepository);

        if (!forceCreation)
            checkIfValidRepositoryOrNonRepositoryFileAlreadyExistsAtGivenLocation(magitRepository.getLocation());
        if (Files.exists(Paths.get(magitRepository.getLocation()))) {
            File repositoryDirectory = new File(magitRepository.getLocation());
            FileUtils.deleteDirectory(repositoryDirectory);
        }
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

    private void checkIfValidRepositoryOrNonRepositoryFileAlreadyExistsAtGivenLocation(String repositoryPath) throws FileAlreadyExistsException {
        if (Files.exists(Paths.get(repositoryPath)) &&
                Files.exists(Paths.get(repositoryPath, ".magit")) &&
                Files.exists(Paths.get(repositoryPath, ".magit", "REPOSITORY_NAME")) &&
                Files.exists((Paths.get(repositoryPath, ".magit", "Branches", "HEAD"))))
            throw new RepositoryAlreadyExistsException("Functioning repository already exists at location " + repositoryPath + ".", repositoryPath);
        else if (Files.exists(Paths.get(repositoryPath)))
            throw new FileAlreadyExistsException("There is a non repository file at that location.");
    }

    private void checkIfXmlFile(String pathToXml) throws XmlFileException, IllegalPathException {
        try {
            if (!Paths.get(pathToXml).getFileName().toString().endsWith(".xml"))
                throw new XmlFileException("XML error : Given file is not xml file, file does not end with .xml.");
        } catch (InvalidPathException e) {
            throw new IllegalPathException(pathToXml + " is not a valid path.");
        }
    }

    private void checkXmlFoldersForWrongItemIdPointers(MagitRepository magitRepository) throws XmlFileException {
        List<MagitSingleFolder> magitSingleFolders = magitRepository.getMagitFolders().getMagitSingleFolder();
        for (MagitSingleFolder magitSingleFolder : magitSingleFolders) {
            for (Item item : magitSingleFolder.getItems().getItem()) {
                checkIfItemIdExists(magitRepository, magitSingleFolder, item);
            }
        }
    }

    private void checkXmlCommitsForWrongFolderIdPointers(MagitRepository magitRepository) throws XmlFileException {
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
            if (magitRepository.getMagitBlobs().getMagitBlob()
                    .stream()
                    .noneMatch(blob -> blob.getId().equals(itemTCheck.getId())))
                throw new XmlFileException("XML error : Folder id " + magitSingleFolder.getId() + " contains the id " + itemTCheck.getId() + " of blob that doesn't exist.");
        } else if (itemTCheck.getType().equals("folder")) {
            if (magitRepository.getMagitFolders().getMagitSingleFolder()
                    .stream()
                    .noneMatch(folder -> folder.getId().equals(itemTCheck.getId())))
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
            if (magitSingleCommits.stream().noneMatch(magitCommit -> magitCommit.getId().equals(magitSingleBranch.getPointedCommit().getId())) && magitSingleCommits.size() > 0) {
                throw new XmlFileException("XML error : Branch named: '" + magitSingleBranch.getName() + "' has invalid pointed commit id.");
            }
        }
    }

    private void checkXmlHeadBranchExists(MagitRepository magitRepository) throws XmlFileException {
        if (magitRepository.getMagitBranches().getMagitSingleBranch().stream().noneMatch(branch -> branch.getName().equals(magitRepository.getMagitBranches().getHead()))) {
            throw new XmlFileException("XML error : Head branch '" + magitRepository.getMagitBranches().getHead() + "' does not exist.");
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
        magitFolders.getMagitSingleFolder().stream().filter(MagitSingleFolder::isIsRoot)
                .forEach(folder -> insertFileItemsToTree(magitFolders, folder, treeMap, blobMap));
    }

    private void insertFileItemsToTree(MagitFolders magitFolders, MagitSingleFolder folder,
                                       HashMap<String, Tree> treeMap, HashMap<String, Blob> blobMap) {
        for (Item item : folder.getItems().getItem()) {
            if (item.getType().equals("blob"))
                treeMap.get(folder.getId()).addFileItem(blobMap.get(item.getId()));
            else if (item.getType().equals("folder")) {
                MagitSingleFolder nextFolderInHierarchy = magitFolders.getMagitSingleFolder().stream()
                        .filter(f -> f.getId().equals(item.getId())).findFirst().get();
                insertFileItemsToTree(magitFolders, nextFolderInHierarchy, treeMap, blobMap);
                treeMap.get(folder.getId()).addFileItem(treeMap.get(item.getId()));
            }
        }
    }

    private ArrayList<Commit> createCommitsInstances(MagitRepository magitRepository, HashMap<String, Tree> folders) throws ParseException, PreviousCommitsLimitExceededException, IOException {
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
                commitsOfRepository.get(magitCommit.getId())
                        .addPreceding(commitsOfRepository.get(precedingCommit.getId()).getSha1Code().toString());
            }
        }
        return new ArrayList<>(commitsOfRepository.values());
    }

    private void createBranches(MagitRepository magitRepository, Repository repository,
                                BranchManager branchManager, ArrayList<Commit> commits) {
        String headBranchName = magitRepository.getMagitBranches().getHead();
        for (MagitSingleBranch branch : magitRepository.getMagitBranches().getMagitSingleBranch()) {
            Sha1 branchContent = new Sha1("", true);
            if (branch.getPointedCommit().getId() != "") {
                int indexOfCommit = Integer.parseInt(branch.getPointedCommit().getId()) - 1;
                branchContent = new Sha1(commits.get(indexOfCommit).getSha1Code().toString(), true);
            }

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

    public void writeRepositoryToXML(Repository repository, String saveFileTo)
            throws IOException, ParseException, PreviousCommitsLimitExceededException, JAXBException {

        blobSha1ToId = new HashMap<>();
        treeSha1ToId = new HashMap<>();

        MagitRepository magitRepository = createMagitRepository(repository);
        HashMap<String, String> sha1OfCommits = buildObjectsFromCommits(repository, magitRepository);
        magitRepository.setMagitBranches(createMagitBranches(repository, sha1OfCommits));

        JAXBContext jaxbContext = JAXBContext.newInstance("com.magit.logic.system.XMLObjects");

        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        FileOutputStream fileOutputStream = new FileOutputStream(saveFileTo);
        marshaller.marshal(magitRepository, fileOutputStream);
        fileOutputStream.close();
    }

    private MagitRepository createMagitRepository(Repository repository) {
        MagitRepository magitRepository = new MagitRepository();
        magitRepository.setMagitFolders(new MagitFolders());
        magitRepository.setMagitBlobs(new MagitBlobs());
        magitRepository.setMagitCommits(new MagitCommits());
        magitRepository.setLocation(repository.getRepositoryPath().toString());
        magitRepository.setName(repository.getRepositoryName());
        return magitRepository;
    }

    private HashMap<String, String> buildObjectsFromCommits(Repository repository, MagitRepository magitRepository)
            throws IOException, ParseException, PreviousCommitsLimitExceededException {
        HashMap<String, String> sha1ToId = mapCommitSha1ToId(repository);
        ArrayList<MagitSingleCommit> magitCommitsList = new ArrayList<>();
        ArrayList<MagitSingleFolder> magitSingleFolders = new ArrayList<>();
        ArrayList<MagitBlob> magitBlobs = new ArrayList<>();
        StringBuilder sha1OfFiles = new StringBuilder();

        for (String sha1OfCommit : sha1ToId.keySet()) {
            Commit currentCommit = Commit.createCommitInstanceByPath(
                    Paths.get(repository.getObjectsFolderPath().toString(), sha1OfCommit));
            if (currentCommit == null)
                continue;

            MagitSingleCommit currentMagitCommit = analyzeCommitToMagitSingleCommit(currentCommit, sha1ToId);
            magitCommitsList.add(currentMagitCommit);
            Tree foldersOfCommit = WorkingCopyUtils.getWorkingCopyTreeFromCommit(currentCommit, repository.getRepositoryPath().toString());
            createMagitObjectsFromTree(sha1OfFiles, foldersOfCommit, magitBlobs, magitSingleFolders);
            RootFolder rootFolder = new RootFolder();
            String idOfRootFolder = magitSingleFolders.size() > 0 ? magitSingleFolders.get(0).getId() : null;
            if (idOfRootFolder != null)
                rootFolder.setId(idOfRootFolder);
            currentMagitCommit.setRootFolder(rootFolder);
        }
        insertObjectToMagitRepository(magitRepository, magitBlobs, magitSingleFolders, magitCommitsList);
        return sha1ToId;
    }

    private HashMap<String,String> mapCommitSha1ToId(Repository repository)throws IOException {
        HashMap<String,String> sha1toId = new HashMap<>();
        Integer id = 1;
        for (String sha1 : repository.getAllCommitsOfRepository()) {
            sha1toId.put(sha1, id.toString());
            id++;
        }

        return sha1toId;
    }

    private MagitSingleCommit analyzeCommitToMagitSingleCommit(Commit commit, HashMap<String, String> sha1ToId) {
        int id = Integer.parseInt(sha1ToId.get(commit.getSha1Code().toString()));
        MagitSingleCommit currentMagitCommit = MagitObjectsFactory.createMagitSingleCommit(commit, id);
        setPrecedingCommits(commit, currentMagitCommit, sha1ToId);

        return currentMagitCommit;
    }

    private void insertObjectToMagitRepository(MagitRepository magitRepository, ArrayList<MagitBlob> magitBlobs,
                                               ArrayList<MagitSingleFolder> magitSingleFolders, ArrayList<MagitSingleCommit> magitCommitsList) {
        MagitBlobs magitBlobCollection = new MagitBlobs();
        magitBlobCollection.getMagitBlob().addAll(magitBlobs);
        magitRepository.setMagitBlobs(magitBlobCollection);
        MagitFolders magitFolders = new MagitFolders();
        magitFolders.getMagitSingleFolder().addAll(magitSingleFolders);
        magitRepository.setMagitFolders(magitFolders);
        MagitCommits magitCommits = MagitObjectsFactory.createMagitCommits();
        magitCommits.getMagitSingleCommit().addAll(magitCommitsList);
        magitRepository.setMagitCommits(magitCommits);
    }


    private void createMagitObjectsFromTree(StringBuilder sha1OfFiles, Tree folder, ArrayList<MagitBlob> magitBlobsList,
                                            ArrayList<MagitSingleFolder> magitFoldersList) {
        final int empty = 0;
        LinkedList<FileItem> objectsOfTree = new LinkedList<>();
        objectsOfTree.add(folder);

        while (objectsOfTree.size() != empty) {
            handleFileItem(objectsOfTree, sha1OfFiles, blobSha1ToId, treeSha1ToId, magitBlobsList,magitFoldersList);
        }
        MagitBlobs magitBlobs = MagitObjectsFactory.createMagitBlobs();
        magitBlobs.getMagitBlob().addAll(magitBlobsList);
        MagitFolders magitFolders = MagitObjectsFactory.createMagitFolders();
        magitFolders.getMagitSingleFolder().addAll(magitFoldersList);
    }

    private void handleFileItem(LinkedList<FileItem> objectsOfTree, StringBuilder sha1OfFiles,
                                HashMap<String, String> blobSha1ToId, HashMap<String, String> treeSha1ToId,
                                ArrayList<MagitBlob> magitBlobsList, ArrayList<MagitSingleFolder> magitFoldersList) {
        Integer idTree = treeSha1ToId.size() + 1;
        FileItem item = objectsOfTree.poll();
        String sha1Code = item.getSha1Code().toString();
        if (sha1OfFiles.toString().contains(sha1Code + item.getmName()))
            return;

        sha1OfFiles.append(String.format("%s%s%s", sha1Code, item.getmName(), ';'));
        if (item.getmFileType().equals(FileType.FILE)) {
            String id = blobSha1ToId.get(sha1Code);
            magitBlobsList.add(MagitObjectsFactory.createMagitBlob((Blob) item, Integer.parseInt(id)));
        } else if (item.getmFileType().equals(FileType.FOLDER)) {
            if (!treeSha1ToId.containsKey(sha1Code)) {
                treeSha1ToId.put(sha1Code, idTree.toString());
            }
            String id = treeSha1ToId.get(sha1Code);
            MagitSingleFolder magitSingleFolder = MagitObjectsFactory.createMagitSingleFolder((Tree) item, Integer.parseInt(id), item.getmName() == null);

            ArrayList<Item> itemsOfFolder = new ArrayList<>();
            for (FileItem childInFolder : ((Tree)item).getmFiles()) {
                Item itemToAdd = generateItemFromChild(childInFolder);
                if (itemToAdd != null) {
                    itemsOfFolder.add(itemToAdd);
                }
                objectsOfTree.add(childInFolder);
            }
            MagitSingleFolder.Items items = MagitObjectsFactory.createItemList();
            items.getItem().addAll(itemsOfFolder);
            magitSingleFolder.setItems(items);
            magitFoldersList.add(magitSingleFolder);
        }
    }

    private Item generateItemFromChild(FileItem childInFolder) {

        String sha1OfChild = childInFolder.getSha1Code().toString();

        if (childInFolder.getmFileType().equals(FileType.FILE)) {
            boolean containsKey = blobSha1ToId.containsKey(sha1OfChild);
            Integer idBlob = containsKey ? Integer.parseInt(blobSha1ToId.get(sha1OfChild)) : blobSha1ToId.size() + 1;
            if (!containsKey) {
                blobSha1ToId.put(childInFolder.getSha1Code().toString(), idBlob.toString());
            }
            return MagitObjectsFactory.createItem(idBlob, "blob");
        } else if (childInFolder.getmFileType().equals(FileType.FOLDER)) {
            boolean containsKey = treeSha1ToId.containsKey(sha1OfChild);
            Integer idTree = containsKey ? Integer.parseInt(treeSha1ToId.get(sha1OfChild)): treeSha1ToId.size() + 1;
            if (!containsKey) {
                treeSha1ToId.put(childInFolder.getSha1Code().toString(), idTree.toString());
            }
            return MagitObjectsFactory.createItem(idTree, "folder");
        }
        return null;
    }

    private void setPrecedingCommits(Commit commit, MagitSingleCommit magitSingleCommit,
                                     HashMap<String, String> sha1ToId) {
        ArrayList<PrecedingCommits.PrecedingCommit> precedingCommitsCollection = new ArrayList<>();
        boolean hasNewSha1 = false;
        for (Sha1 sha1Code : commit.getLastCommitsSha1Codes()) {
            if (sha1Code.toString().equals(""))
                continue;

            hasNewSha1 = true;
            PrecedingCommits.PrecedingCommit precedingCommit = new PrecedingCommits.PrecedingCommit();
            precedingCommit.setId(sha1ToId.get(sha1Code.toString()));
            precedingCommitsCollection.add(precedingCommit);
        }
        PrecedingCommits pc =new PrecedingCommits();
        magitSingleCommit.setPrecedingCommits(pc);
        if (hasNewSha1) {
            pc.getPrecedingCommit().addAll(precedingCommitsCollection);
        }
    }

    private MagitBranches createMagitBranches(Repository repository, HashMap<String,String> sha1ToId) throws IOException {
        if (Files.notExists(repository.getBranchDirectoryPath()))
            return null;

        File branchesDir = new File(repository.getBranchDirectoryPath().toString());
        File[] children = branchesDir.listFiles();
        if (children == null)
            return null;

        MagitBranches magitBranches = new MagitBranches();

        for (File child : children) {

            if (child.getName().equals("HEAD")) {
                String headBranchName = FileHandler.readFile(repository.getHeadPath().toString());
                magitBranches.setHead(headBranchName);
            }
            else {
                MagitSingleBranch magitSingleBranch = new MagitSingleBranch();
                MagitSingleBranch.PointedCommit pointedCommit = new MagitSingleBranch.PointedCommit();
                magitSingleBranch.setName(child.getName());
                String key = FileHandler.readFile(child.getAbsolutePath());
                if (sha1ToId != null && sha1ToId.containsKey(key))
                    pointedCommit.setId(sha1ToId.get(key));
                else
                    pointedCommit.setId("");
                magitSingleBranch.setPointedCommit(new MagitSingleBranch.PointedCommit());
                magitSingleBranch.setPointedCommit(pointedCommit);
                magitBranches.getMagitSingleBranch().add(magitSingleBranch);
            }
        }
        return magitBranches;
    }
}
