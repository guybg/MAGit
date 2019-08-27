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
import java.util.*;

public class RepositoryXmlParser {
    private HashMap<String, String> blobSha1AndNameToId;
    private HashMap<String, String> treeSha1AndNameToId;
    private MagitRepository magitRepository;

    private HashMap<String, Blob> blobMap;
    private HashMap<String, Tree> treeMap;
    private Repository repository = null;
    private ArrayList<Commit> commits;

    public RepositoryXmlParser (String xmlPath) throws JAXBException, IOException,XmlFileException, IllegalPathException{
        checkIfXmlFile(xmlPath);

        JAXBContext jaxbContext = JAXBContext.newInstance("com.magit.logic.system.XMLObjects");

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        FileInputStream xmlStream = new FileInputStream(new File(xmlPath));
        StreamSource streamSource = new StreamSource(xmlStream);

        magitRepository = unmarshaller.unmarshal(streamSource, MagitRepository.class).getValue();
    }

    public int getObjectsCount() {
        int referenceCount = 1, repositoryCount = 1;
        return magitRepository.getMagitBlobs().getMagitBlob().size() +
                magitRepository.getMagitBranches().getMagitSingleBranch().size() +
                magitRepository.getMagitCommits().getMagitSingleCommit().size() +
                magitRepository.getMagitFolders().getMagitSingleFolder().size() +
                referenceCount + repositoryCount;
    }

    public void checkXmlValidity() throws XmlFileException {
        checkXmlForDuplicateRepositoryItemIdsError(magitRepository);
        checkXmlFoldersForWrongItemIdPointers(magitRepository);
        checkXmlCommitsForWrongFolderIdPointers(magitRepository);
        checkXmlForInvalidPointedCommitsInBranches(magitRepository);
        checkXmlHeadBranchExists(magitRepository);
        checkXmlRemoteRepositoryLocation(magitRepository);
        checkXmlBranchesTracking(magitRepository);
    }

    public void handleExistingRepositories(boolean forceCreation) throws IOException, RepositoryAlreadyExistsException {
        if (!forceCreation)
            checkIfValidRepositoryOrNonRepositoryFileAlreadyExistsAtGivenLocation(magitRepository.getLocation());
        if (Files.exists(Paths.get(magitRepository.getLocation()))) {
            File repositoryDirectory = new File(magitRepository.getLocation());
            FileUtils.deleteDirectory(repositoryDirectory);
        }
    }

    public Integer importBlobs() throws ParseException {
        blobMap = createBlobMap(magitRepository);
        return blobMap.size();
    }

    public Integer importFolders() throws ParseException {
        treeMap = createFolderMap(magitRepository);
        return treeMap.size();
    }

    public Integer createCommits() throws ParseException, PreviousCommitsLimitExceededException, IOException {
        commits =  createCommitsInstances(magitRepository, treeMap);
        return commits.size();
    }

   public void initializeRepository(){
        this.repository =  new Repository(Paths.get(magitRepository.getLocation()).toString(), magitRepository.getName());
    }

    public Integer createBranches(BranchManager branchManager){
        return createBranches(magitRepository, repository, branchManager, commits);
    }

    public Repository createRepository() throws IOException, IllegalPathException {
        repository.create();
        zipCommitWorkingCopy(repository, commits, treeMap);

        return repository;
    }

    private void checkIfValidRepositoryOrNonRepositoryFileAlreadyExistsAtGivenLocation(String repositoryPath) throws FileAlreadyExistsException, RepositoryAlreadyExistsException {
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
            if (!Paths.get(pathToXml).toAbsolutePath().getFileName().toString().endsWith(".xml"))
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
            checkIfValidCommitFolder(magitRepository, magitSingleCommit.getRootFolder());
        }
    }

    private void checkIfValidCommitFolder(MagitRepository magitRepository, RootFolder rootFolder) throws XmlFileException {
        if (magitRepository.getMagitFolders().getMagitSingleFolder().stream().noneMatch(magitFolder -> magitFolder.getId().equals(rootFolder.getId()) && magitFolder.isIsRoot())) {
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

    private void checkXmlRemoteRepositoryLocation(MagitRepository magitRepository) throws XmlFileException {
        MagitRepository.MagitRemoteReference remoteReference = magitRepository.getMagitRemoteReference();
        if(remoteReference == null || remoteReference.getLocation() == null && remoteReference.getName() == null)
            return;
        if(remoteReference.getLocation() == null || remoteReference.getName() == null){
            if(remoteReference.getLocation() == null)
                throw new XmlFileException("XML Error : repository has remote reference, but remote repository location not set.");
            if(remoteReference.getName() == null)
                throw new XmlFileException("XML Error : repository has remote reference, but remote repository name not set.");
        }
        if (Files.notExists(Paths.get(remoteReference.getLocation())))
            throw new XmlFileException("XML Error : Remote reference location does not exist.");
    }

    private void checkXmlBranchesTracking(MagitRepository magitRepository) throws XmlFileException {
        HashMap<String, MagitSingleBranch> singleBranchMap = new HashMap<>();
        for (MagitSingleBranch branch : magitRepository.getMagitBranches().getMagitSingleBranch()) {
            singleBranchMap.put(branch.getName(), branch);
        }

        for (Map.Entry<String, MagitSingleBranch> branchEntry : singleBranchMap.entrySet()) {
            MagitSingleBranch branch = branchEntry.getValue();
            String trackingAfter = branch.getTrackingAfter();
            if (branch.isTracking() &&
            (!singleBranchMap.containsKey(trackingAfter) || !singleBranchMap.get(trackingAfter).isIsRemote())) {
                throw new XmlFileException("XML Error : Branch tracking is invalid.");
            }
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

    public void buildTree() {
        insertFileItemsToTrees(magitRepository.getMagitFolders(), treeMap, blobMap);
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

    private Integer createBranches(MagitRepository magitRepository, Repository repository,
                                BranchManager branchManager, ArrayList<Commit> commits) {
        Integer branchCount = 0;
        String headBranchName = magitRepository.getMagitBranches().getHead();
        for (MagitSingleBranch branch : magitRepository.getMagitBranches().getMagitSingleBranch()) {
            Sha1 branchContent = new Sha1("", true);
            if (!branch.getPointedCommit().getId().equals("")) {
                int indexOfCommit = Integer.parseInt(branch.getPointedCommit().getId()) - 1;
                branchContent = new Sha1(commits.get(indexOfCommit).getSha1Code().toString(), true);
            }

            if (branch.getName().equals(headBranchName)) {
                Branch headBranch = new Branch(branch.getName(), branchContent.toString(),
                        branch.getTrackingAfter(), branch.isIsRemote(), branch.isTracking());
                branchManager.setActiveBranch(headBranch);
                repository.addBranch(headBranch.getBranchName(), headBranch);
                repository.addBranch("HEAD", headBranch);
            } else {
                Branch branchToAdd = new Branch(branch.getName(), branchContent.toString(),
                        branch.getTrackingAfter(), branch.isIsRemote(), branch.isTracking());
                repository.addBranch(branchToAdd.getBranchName(), branchToAdd);
            }
            branchCount++;
        }
        return branchCount;
    }

    private void zipCommitWorkingCopy(Repository repository, ArrayList<Commit> commits, HashMap<String, Tree> treeMap)
            throws IOException {
        for (Commit commit : commits) {
            commit.generateCommitFile(repository.getObjectsFolderPath());
            Sha1 commitWorkingCopySha1commit = commit.getWorkingCopySha1();
            for (Tree treeEntry : treeMap.values()) {
                if (treeEntry.getSha1Code().equals(commitWorkingCopySha1commit)) {
                    WorkingCopyUtils workingCopyUtils = new WorkingCopyUtils(repository.getRepositoryPath().toString(),
                            treeEntry.getLastUpdater(), treeEntry.getLastModified());
                    workingCopyUtils.zipWorkingCopyFromTreeWC(treeEntry);
                }
            }
        }
    }

    public void writeRepositoryToXML(Repository repository, String saveFileTo)
            throws IOException, ParseException, PreviousCommitsLimitExceededException, JAXBException {

        blobSha1AndNameToId = new HashMap<>();
        treeSha1AndNameToId = new HashMap<>();

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
            ArrayList<MagitSingleFolder> magitSingleFoldersOfCommit = new ArrayList<>();
            Commit currentCommit = Commit.createCommitInstanceByPath(
                    Paths.get(repository.getObjectsFolderPath().toString(), sha1OfCommit));
            if (currentCommit == null)
                continue;

            MagitSingleCommit currentMagitCommit = analyzeCommitToMagitSingleCommit(currentCommit, sha1ToId);
            magitCommitsList.add(currentMagitCommit);
            Tree foldersOfCommit = WorkingCopyUtils.getWorkingCopyTreeFromCommit(currentCommit, repository.getRepositoryPath().toString());
            createMagitObjectsFromTree(sha1OfFiles, foldersOfCommit, magitBlobs, magitSingleFoldersOfCommit);
            RootFolder rootFolder = new RootFolder();
            String idOfRootFolder = magitSingleFoldersOfCommit.size() > 0 ? magitSingleFoldersOfCommit.get(0).getId() : null;
            if (idOfRootFolder != null)
                rootFolder.setId(idOfRootFolder);
            currentMagitCommit.setRootFolder(rootFolder);
            magitSingleFolders.addAll(magitSingleFoldersOfCommit);
        }
        insertObjectToMagitRepository(magitRepository, magitBlobs, magitSingleFolders, magitCommitsList);
        return sha1ToId;
    }

    private HashMap<String, String> mapCommitSha1ToId(Repository repository) throws IOException {
        HashMap<String, String> sha1toId = new HashMap<>();
        Integer id = 1;
        String[] commitsSha1CodesOfRepository = repository.getAllCommitsOfRepository();
        if (commitsSha1CodesOfRepository != null)
            for (String sha1 : commitsSha1CodesOfRepository) {
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
            handleFileItem(objectsOfTree, sha1OfFiles, blobSha1AndNameToId, treeSha1AndNameToId, magitBlobsList, magitFoldersList);
        }
        MagitBlobs magitBlobs = MagitObjectsFactory.createMagitBlobs();
        magitBlobs.getMagitBlob().addAll(magitBlobsList);
        MagitFolders magitFolders = MagitObjectsFactory.createMagitFolders();
        magitFolders.getMagitSingleFolder().addAll(magitFoldersList);
    }

    private void handleFileItem(LinkedList<FileItem> objectsOfTree, StringBuilder sha1OfFiles,
                                HashMap<String, String> blobSha1AndNameToId, HashMap<String, String> treeSha1AndNameToId,
                                ArrayList<MagitBlob> magitBlobsList, ArrayList<MagitSingleFolder> magitFoldersList) {
        int idTree = treeSha1AndNameToId.size() + 1;
        FileItem item = objectsOfTree.poll();
        String sha1Code = item.getSha1Code().toString();
        if (sha1OfFiles.toString().contains(sha1Code + item.getName()))
            return;

        sha1OfFiles.append(String.format("%s%s%s", sha1Code, item.getName(), ';'));
        if (item.getFileType().equals(FileType.FILE)) {
            String id = blobSha1AndNameToId.get(sha1Code + item.getName());
            magitBlobsList.add(MagitObjectsFactory.createMagitBlob((Blob) item, Integer.parseInt(id)));
        } else if (item.getFileType().equals(FileType.FOLDER)) {
            if (!treeSha1AndNameToId.containsKey(sha1Code + item.getName())) {
                treeSha1AndNameToId.put(sha1Code + item.getName(), Integer.toString(idTree));
            }
            String id = treeSha1AndNameToId.get(sha1Code + item.getName());
            MagitSingleFolder magitSingleFolder = MagitObjectsFactory.createMagitSingleFolder((Tree) item, Integer.parseInt(id), item.getName() == null);

            ArrayList<Item> itemsOfFolder = new ArrayList<>();
            for (FileItem childInFolder : ((Tree) item).getFiles()) {
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

        if (childInFolder.getFileType().equals(FileType.FILE)) {
            boolean containsKey = blobSha1AndNameToId.containsKey(sha1OfChild + childInFolder.getName());
            Integer idBlob = containsKey ? Integer.parseInt(blobSha1AndNameToId.get(sha1OfChild + childInFolder.getName())) : blobSha1AndNameToId.size() + 1;
            if (!containsKey) {
                blobSha1AndNameToId.put(childInFolder.getSha1Code().toString() + childInFolder.getName(), idBlob.toString());
            }
            return MagitObjectsFactory.createItem(idBlob, "blob");
        } else if (childInFolder.getFileType().equals(FileType.FOLDER)) {
            boolean containsKey = treeSha1AndNameToId.containsKey(sha1OfChild + childInFolder.getName());
            Integer idTree = containsKey ? Integer.parseInt(treeSha1AndNameToId.get(sha1OfChild + childInFolder.getName())) : treeSha1AndNameToId.size() + 1;
            if (!containsKey) {
                treeSha1AndNameToId.put(childInFolder.getSha1Code().toString() + childInFolder.getName(), idTree.toString());
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
        PrecedingCommits pc = new PrecedingCommits();
        magitSingleCommit.setPrecedingCommits(pc);
        if (hasNewSha1) {
            pc.getPrecedingCommit().addAll(precedingCommitsCollection);
        }
    }

    private MagitBranches createMagitBranches(Repository repository, HashMap<String, String> sha1ToId) throws IOException {
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
            } else {
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

    public void setRemoteReference() {
        MagitRepository.MagitRemoteReference reference = magitRepository.getMagitRemoteReference();
        if (null == reference)
            return;

        RemoteReference remoteReference = new RemoteReference(reference.getName(), reference.getLocation());
        repository.setRemoteReference(remoteReference);
    }
}
