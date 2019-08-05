import com.magit.logic.exceptions.*;
import com.magit.logic.system.MagitEngine;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class UserInterface {

    private final static String UPDATE_USER_NAME = "Update User Name";
    private final static String LOAD_REPOSITORY_FROM_XML = "Load Repository From XML";
    private final static String EXPORT_REPOSITORY_TO_XML = "Export Repository To XML";
    private final static String SWITCH_REPOSITORY = "Load/Switch Repository";
    private final static String PRESENT_CURRENT_COMMIT_AND_HISTORY = "Present Current Commit and History";
    private final static String SHOW_WORKING_COPY_STATUS = "Show Working Copy Status";
    private final static String Commit = "Commit";
    private final static String PRESENT_ALL_BRANCHES = "Present All Branches";
    private final static String CREATE_NEW_REPOSITORY = "Create New Repository";
    private final static String CREATE_NEW_BRANCH = "Create New Branch";
    private final static String DELETE_BRANCH = "Delete Branch";
    private final static String PICK_HEAD_BRANCH = "Checkout";
    private final static String PRESENT_CURRENT_BRANCH_HISTORY = "Present Current Branch History";
    private final static String CHANGE_HEAD_BRANCH_POINTED_COMMIT = "Change Head Branch Pointed Commit";
    private final static String EXIT = "Exit";

    public static void main(String[] args) {
        MagitEngine maGitSystem = new MagitEngine();
        try {
            run(maGitSystem);
        } catch (RepositoryAlreadyExistsException e) {
            System.out.println(e.getMessage() + "\n" +
                    e.getCause());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private static void printMenu() {
        System.out.println("Menu:" + System.lineSeparator() +
                "1." + UPDATE_USER_NAME + System.lineSeparator() +
                "2." + LOAD_REPOSITORY_FROM_XML + System.lineSeparator() +
                "3." + EXPORT_REPOSITORY_TO_XML + System.lineSeparator() +
                "4." + SWITCH_REPOSITORY + System.lineSeparator() +
                "5." + PRESENT_CURRENT_COMMIT_AND_HISTORY + System.lineSeparator() +
                "6." + SHOW_WORKING_COPY_STATUS + System.lineSeparator() +
                "7." + Commit + System.lineSeparator() +
                "8." + PRESENT_ALL_BRANCHES + System.lineSeparator() +
                "9." + CREATE_NEW_REPOSITORY + System.lineSeparator() +
                "10." + CREATE_NEW_BRANCH + System.lineSeparator() +
                "11." + DELETE_BRANCH + System.lineSeparator() +
                "12." + PICK_HEAD_BRANCH + System.lineSeparator() +
                "13." + PRESENT_CURRENT_BRANCH_HISTORY + System.lineSeparator() +
                "14." + CHANGE_HEAD_BRANCH_POINTED_COMMIT + System.lineSeparator() +
                "15." + EXIT);
    }

    private static void run(MagitEngine magitEngine) throws
            IOException, ParseException, JAXBException {
        Scanner input = new Scanner(System.in);
        MenuOptions optionsToActivate = MenuOptions.Default;
        do {
            try {
                printMenu();
                optionsToActivate = MenuOptions.getEnumByInt(input.nextInt());
                input.nextLine();
            } catch (InputMismatchException ex) {
                input.nextLine();
                System.out.println("Please choose a number.");
                continue;
            }
            switch (optionsToActivate) {
                case UpdateUserName:
                    updateUserName(magitEngine, input);
                    break;
                case LoadRepositoryFromXML:
                    loadRepositoryFromXML(magitEngine, input);
                    break;
                case ExportRepositoryToXML:
                    exportRepositoryToXML(magitEngine, input);
                    break;
                case SwitchRepository:
                    switchRepository(magitEngine, input);
                    break;
                case PresentCurrentCommitAndHistory:
                    presentCurrentCommitAndHistory(magitEngine);
                    break;
                case ShowWorkingCopyStatus:
                    showWorkingCopyStatus(magitEngine);
                    break;
                case Commit:
                    commit(magitEngine, input);
                    break;
                case PresentAllBranches:
                    presentAllBranches(magitEngine);
                    break;
                case CreateNewRepository:
                    try {
                        createNewRepository(magitEngine, input);
                    } catch (IllegalPathException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case CreateNewBranch:
                    createNewBranch(magitEngine, input);
                    break;
                case DeleteBranch:
                    deleteBranch(magitEngine, input);
                    break;
                case PickHeadBranch:
                    pickHeadBranch(magitEngine, input);
                    break;
                case PresentCurrentBranchHistory:
                    presentCurrentBranchHistory(magitEngine);
                    break;
                case ChangePointedCommitSha1:
                    changePointedCommitSha1(magitEngine, input);
                    break;
                case Exit:
                    optionsToActivate = MenuOptions.Exit;
                    break;
                case Default:
                default:
                    System.out.println("Please choose a number in the above range");
                    break;
            }
        } while (!optionsToActivate.equals(MenuOptions.Exit));
    }

    private static void loadRepositoryFromXML(MagitEngine magitEngine, Scanner input) throws ParseException, JAXBException {
        System.out.println("Please enter xml file path:");
        String xmlPath = "";
        try {
            xmlPath = input.nextLine();
            magitEngine.loadRepositoryFromXML(xmlPath, false);
        } catch (RepositoryAlreadyExistsException e) {
            System.out.println(e.getMessage());
            if (yesNoQuestion("Would you like to replace current repository with XML repository?, press y/Y to replace, any other key to abort.", input)) {
                try {
                    magitEngine.loadRepositoryFromXML(xmlPath, true);
                } catch (IOException | PreviousCommitsLimitExceededException | XmlFileException | IllegalPathException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        } catch (PreviousCommitsLimitExceededException | XmlFileException | IllegalPathException | FileAlreadyExistsException | FileNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void exportRepositoryToXML(MagitEngine magitEngine, Scanner input) {
        try {
            magitEngine.repositoryNotFoundCheck();
            System.out.println("Enter destination of XML file:");
            magitEngine.exportRepositoryToXML(input.nextLine());
        } catch (RepositoryNotFoundException | IOException | ParseException | PreviousCommitsLimitExceededException | JAXBException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void updateUserName(MagitEngine magitEngine, Scanner input) {
        System.out.println("Please enter user name:");
        try {
            magitEngine.updateUserName(input.nextLine());
        } catch (InvalidNameException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void presentCurrentCommitAndHistory(MagitEngine magitEngine) throws ParseException {
        try {
            System.out.println(magitEngine.presentCurrentCommitAndHistory());
        } catch (CommitNotFoundException e) {
            System.out.println(e.toString());
        } catch (RepositoryNotFoundException | PreviousCommitsLimitExceededException | IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void switchRepository(MagitEngine magitEngine, Scanner input) throws ParseException {
        try {
            executeSwitchRepository(magitEngine, input);
        } catch (IllegalPathException | InvalidNameException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void showWorkingCopyStatus(MagitEngine magitEngine) throws ParseException {
        try {
            System.out.println(magitEngine.getWorkingCopyStatus());
        } catch (RepositoryNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (PreviousCommitsLimitExceededException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void commit(MagitEngine magitEngine, Scanner input) throws ParseException {
        try {
            magitEngine.repositoryNotFoundCheck();
            System.out.println("Please enter your commit message ");
            magitEngine.commit(input.nextLine());
        } catch (WorkingCopyIsEmptyException e) {
            System.out.println(e.toString());
        } catch (WorkingCopyStatusNotChangedComparedToLastCommitException e) {
            System.out.println(e.toString());
        } catch (RepositoryNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (PreviousCommitsLimitExceededException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void presentAllBranches(MagitEngine magitEngine) throws ParseException {
        try {
            System.out.println(magitEngine.getBranchesInfo());
        } catch (RepositoryNotFoundException | PreviousCommitsLimitExceededException | IOException e) {
            System.out.println(e.getMessage());
        }

    }

    private static void createNewRepository(MagitEngine magitEngine, Scanner input) throws IllegalPathException {
        System.out.println("Please enter path:");
        String pathToRepositoryInput = input.nextLine();
        try {
            Path pathToRepository = Paths.get(pathToRepositoryInput);
            System.out.println("Please enter repository name");
            magitEngine.createNewRepository(pathToRepository, input.nextLine());
        } catch (InvalidNameException e) {
            System.out.println(e.getMessage());
        } catch (InvalidPathException e) {
            throw new IllegalPathException(pathToRepositoryInput + " is not a valid path.");
        }
    }

    private static void createNewBranch(MagitEngine magitEngine, Scanner input) throws ParseException {
        try {
            magitEngine.repositoryNotFoundCheck();
            System.out.println("Pick branch name:");
            String branchName = input.nextLine();
            magitEngine.createNewBranch(branchName);
            if (yesNoQuestion("Would you like to perform checkout to this branch right now? press Y/y to perform checkout or press any other key to cancel",
                    input)) {
                changeBranchOperation(magitEngine, branchName, input);
            }
        } catch (RepositoryNotFoundException | InvalidNameException | BranchAlreadyExistsException | IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void deleteBranch(MagitEngine magitEngine, Scanner input) {
        try {
            magitEngine.repositoryNotFoundCheck();
            System.out.println("Enter branch name:");
            magitEngine.deleteBranch(input.nextLine());
        } catch (RepositoryNotFoundException | ActiveBranchDeletedException | BranchNotFoundException | IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void presentCurrentBranchHistory(MagitEngine magitEngine) throws ParseException {
        try {
            System.out.println(magitEngine.presentCurrentBranch());
        } catch (RepositoryNotFoundException | PreviousCommitsLimitExceededException | IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void changePointedCommitSha1(MagitEngine magitEngine, Scanner input) throws ParseException {
        String commitSha1Code = "";
        try {
            magitEngine.repositoryNotFoundCheck();
            System.out.println("Please Enter Commit's Sha1 code:");
            commitSha1Code = input.nextLine();
            magitEngine.workingCopyChangedComparedToCommit();
            System.out.println(magitEngine.changeBranchPointedCommit(commitSha1Code));
        } catch (PreviousCommitsLimitExceededException ignored) {
        } catch (UncommitedChangesException e) {
            if (yesNoQuestion("Are you sure you want to change pointed commit before committing unsaved changes?, press y/Y to change pointed commit, any other key to abort.", input)) {
                try {
                    System.out.println(magitEngine.changeBranchPointedCommit(commitSha1Code));
                } catch (CommitNotFoundException | RepositoryNotFoundException ex) {
                    System.out.println(ex.getMessage());
                } catch (PreviousCommitsLimitExceededException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.getMessage();
                }
            }
        } catch (CommitNotFoundException | IOException | RepositoryNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }


    private static void executeSwitchRepository(MagitEngine magitEngine, Scanner input) throws ParseException, IllegalPathException, InvalidNameException {
        System.out.println("Please enter repository path:" + System.lineSeparator());
        String pathOfRepositoryString = input.nextLine();
        try {
            Path pathOfRepository = Paths.get(pathOfRepositoryString);
            magitEngine.switchRepository(pathOfRepository.toString());
        } catch (RepositoryNotFoundException ex) {
            if (yesNoQuestion("Repository not found, would you like to create one? Press Y/y to create one, any other button to cancel operation.",
                    input)) {
                System.out.println("Please enter repository name");
                magitEngine.createNewRepository(Paths.get(pathOfRepositoryString), input.nextLine());
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (InvalidPathException e) {
            throw new IllegalPathException(pathOfRepositoryString + " is not a legal path.");
        }
    }

    private static Boolean yesNoQuestion(String message, Scanner inputScanner) {
        boolean result = false;
        System.out.println(message);
        String answer = inputScanner.nextLine();
        if (answer.equals("Y") || answer.equals("y")) {
            result = true;
        }
        return result;
    }

    private static void pickHeadBranch(MagitEngine magitEngine, Scanner input) throws IOException, ParseException {
        try {
            magitEngine.repositoryNotFoundCheck();
            System.out.println("Please enter branch name:");
            changeBranchOperation(magitEngine, input.nextLine(), input);
        } catch (RepositoryNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void changeBranchOperation(MagitEngine magitEngine, String branchName, Scanner input) throws IOException, ParseException {
        try {
            System.out.println(magitEngine.pickHeadBranch(branchName));
        } catch (RepositoryNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (BranchNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (UncommitedChangesException e) {
            System.out.println(e.getMessage());
            if (yesNoQuestion("Press Y/y to switch, any other button to cancel operation.", input)) {
                try {
                    System.out.println(magitEngine.forcedChangeBranch(branchName));
                } catch (PreviousCommitsLimitExceededException ex) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (PreviousCommitsLimitExceededException e) {
            System.out.println(e.getMessage());
        } catch (InvalidNameException e) {
            System.out.println(e.getMessage());
        }
    }

    private enum MenuOptions {
        UpdateUserName,
        LoadRepositoryFromXML,
        ExportRepositoryToXML,
        SwitchRepository,
        PresentCurrentCommitAndHistory,
        ShowWorkingCopyStatus,
        Commit,
        PresentAllBranches,
        CreateNewRepository,
        CreateNewBranch,
        DeleteBranch,
        PickHeadBranch,
        PresentCurrentBranchHistory,
        ChangePointedCommitSha1,
        Exit,
        Default;

        static MenuOptions getEnumByInt(int index) {
            return index <= 0 || index > values().length - 1 ? Default : values()[index - 1];
        }
    }

}

