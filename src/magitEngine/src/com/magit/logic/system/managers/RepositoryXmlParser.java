package com.magit.logic.system.managers;

import com.magit.logic.exceptions.IllegalPathException;
import com.magit.logic.exceptions.PreviousCommitsLimitexceededException;
import com.magit.logic.exceptions.RepositoryAlreadyExistsException;
import com.magit.logic.exceptions.XmlFileException;
import com.magit.logic.system.XMLObjects.*;
import com.magit.logic.system.objects.*;
import com.magit.logic.utils.digest.Sha1;
import com.magit.logic.utils.file.FileHandler;
import com.magit.logic.utils.file.WorkingCopyUtils;
import org.apache.commons.io.FileUtils;

import javax.naming.LimitExceededException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

public class RepositoryXmlParser {

    public Repository parseXMLToRepository(String xmlPath, BranchManager branchManager, String activeUser, boolean forceCreation)
            throws JAXBException, IOException, ParseException, PreviousCommitsLimitexceededException, XmlFileException, IllegalPathException {
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
            throws ParseException, IOException, PreviousCommitsLimitexceededException, XmlFileException, IllegalPathException {
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
            throw new RepositoryAlreadyExistsException("There is already a repository at " + repositoryPath + ".", repositoryPath);
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
        return new ArrayList<>(commitsOfRepository.values());
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

    private void writeRepositoryToXML(Repository repository)
            throws IOException, ParseException, PreviousCommitsLimitexceededException {

        MagitRepository magitRepository = createMagitRepository(repository);
        HashMap<String,String> sha1ToId = createMagitObjects(repository, magitRepository);
        magitRepository.setMagitBranches(createMagitBranches(repository,sha1ToId, magitRepository));
    }

    private MagitRepository createMagitRepository (Repository repository) {
        MagitRepository magitRepository = new MagitRepository();
        magitRepository.setLocation(repository.getRepositoryPath().toString());
        magitRepository.setName(repository.getRepositoryName());
        return magitRepository;
    }

    private HashMap<String, String> createMagitObjects(Repository repository, MagitRepository magitRepository)
            throws IOException, ParseException, PreviousCommitsLimitexceededException{
        Integer id = 0;
        HashMap<String, String> sha1ToId = mapSha1ToId(repository);
        ArrayList<MagitSingleCommit> magitCommitsList = new ArrayList<>();

        for (String sha1OfCommit : sha1ToId.values()) {
            Commit currentCommit = Commit.createCommitInstanceByPath(
                    Paths.get(repository.getObjectsFolderPath().toString(),sha1OfCommit));
            if (currentCommit == null)
                continue;

            MagitSingleCommit currentMagitCommit = MagitObjectsFactory.createMagitSingleCommit(currentCommit, id);
            magitCommitsList.add(currentMagitCommit);
            sha1ToId.put(sha1OfCommit ,id.toString());
            setPrecedingCommits(currentCommit, currentMagitCommit, sha1ToId);
            Tree foldersOfCommit = WorkingCopyUtils.getWorkingCopyTreeFromCommit(currentCommit, repository.getRepositoryPath().toString());
            createMagitObjectsFromTree(foldersOfCommit, magitRepository);
        }

        MagitCommits magitCommits = MagitObjectsFactory.createMagitCommits();
        magitCommits.getMagitSingleCommit().addAll(magitCommitsList);

        return sha1ToId;
    }

    private HashMap<String,String> mapSha1ToId(Repository repository) throws IOException {
        Integer id = 1;
        HashMap<String,String> sha1ToId = new HashMap<>();
        for (String sha1 : repository.getAllCommitsOfRepository()) {
            sha1ToId.put(sha1, id.toString());
            id++;
        }

        return sha1ToId;
    }

    private void createMagitObjectsFromTree(Tree folder, MagitRepository magitRepository) {
        Integer idBlob = 1, idTree = 1;
        LinkedList<FileItem> objectsOfTree = new LinkedList<>();
        objectsOfTree.add(folder);
        ArrayList<MagitBlob> magitBlobsList = new ArrayList<>();
        ArrayList<MagitSingleFolder> magitFoldersList = new ArrayList<>();

        for (FileItem item : objectsOfTree) {
            if (item instanceof Blob) {
                magitBlobsList.add(MagitObjectsFactory.createMagitBlob((Blob) item, idBlob));
            } else if (item instanceof Tree) {
                MagitSingleFolder magitSingleFolder = MagitObjectsFactory.createMagitSingleFolder((Tree) item, idTree, true);//ask guy
                ArrayList<Item> itemsOfFolder = new ArrayList<>();
                for (FileItem childInFolder : ((Tree) item).getmFiles()) {
                    if (childInFolder instanceof Blob) {
                        itemsOfFolder.add(MagitObjectsFactory.createItem(idBlob++, "blob"));
                    } else if (childInFolder instanceof Tree) {
                        itemsOfFolder.add(MagitObjectsFactory.createItem(idTree++, "folder"));
                    }
                    objectsOfTree.add(childInFolder);
                }
                MagitSingleFolder.Items items = MagitObjectsFactory.createItemList();
                items.getItem().addAll(itemsOfFolder);
                magitSingleFolder.setItems(items);
                magitFoldersList.add(magitSingleFolder);
                objectsOfTree.poll();
            }
        }
        MagitBlobs magitBlobs = MagitObjectsFactory.createMagitBlobs();
        magitBlobs.getMagitBlob().addAll(magitBlobsList);
        magitRepository.setMagitBlobs(magitBlobs);
        MagitFolders magitFolders = MagitObjectsFactory.createMagitFolders();
        magitFolders.getMagitSingleFolder().addAll(magitFoldersList);
        magitRepository.setMagitFolders(magitFolders);
    }

    private void setPrecedingCommits(Commit commit, MagitSingleCommit magitSingleCommit,
                                     HashMap<String, String> sha1ToId) {
        ArrayList<PrecedingCommits.PrecedingCommit> precedingCommitsCollection = new ArrayList<>();
        for (String sha1Code :commit.getPerviousCommits()) {
            PrecedingCommits.PrecedingCommit precedingCommit = new PrecedingCommits.PrecedingCommit();
            precedingCommit.setId(sha1ToId.get(sha1Code));
        }
        PrecedingCommits pc = new PrecedingCommits();
        pc.getPrecedingCommit().addAll(precedingCommitsCollection);
        magitSingleCommit.setPrecedingCommits(pc);
    }

    private MagitBranches createMagitBranches(Repository repository, HashMap<String,String> sha1ToId,
                                              MagitRepository magitRepository) throws IOException {
        if (Files.notExists(repository.getBranchDirectoryPath()))
            return null;

        File branchesDir = new File(repository.getBranchDirectoryPath().toString());
        File[] children = branchesDir.listFiles();
        if (children == null)
            return null;

        MagitBranches magitBranches = new MagitBranches();

        for (File child : children) {
            MagitSingleBranch magitSingleBranch = new MagitSingleBranch();
            MagitSingleBranch.PointedCommit pointedCommit = new MagitSingleBranch.PointedCommit();
            if (child.getName().equals("HEAD")) {
                String headBranchName = FileHandler.readFile(repository.getHeadPath().toString());
                magitRepository.getMagitBranches().setHead(headBranchName);
                magitSingleBranch.setName(headBranchName);
                String branchFilePath = Paths.get(repository.getBranchDirectoryPath().toString(), headBranchName).toString();
                pointedCommit.setId(sha1ToId.get(FileHandler.readFile(FileHandler.readFile(branchFilePath))));
                magitSingleBranch.setPointedCommit(pointedCommit);
            }
            else {
                magitSingleBranch.setName(child.getName());
                pointedCommit.setId(sha1ToId.get(FileHandler.readFile(child.getAbsolutePath())));
                magitSingleBranch.setPointedCommit(pointedCommit);
            }
            magitBranches.getMagitSingleBranch().add(magitSingleBranch);
        }
        return magitBranches;
    }
}
