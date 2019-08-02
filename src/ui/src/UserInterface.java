import com.magit.logic.exceptions.*;
import com.magit.logic.system.MagitEngine;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class UserInterface {

    private final static String UPDATE_USER_NAME = "Update User Name";
    private final static String LOAD_REPOSITORY_FROM_XML = "Load Repository From XML";
    private final static String SWITCH_REPOSITORY = "Switch Repository";
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

    public static void main(String[] args) throws IOException, ParseException {
        MagitEngine maGitSystem = new MagitEngine();
        try {
            run(maGitSystem);
            //maGitSystem.commit();
        } catch (RepositoryAlreadyExistsException e) {
            System.out.println(e.getMessage() + "\n" +
                    e.getCause());

        } catch (IllegalPathException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e);
        } //catch (RepositoryNotFoundException e) {
        //catch (WorkingCopyIsEmptyException e) {
        //  e.printStackTrace();
        //}
        catch (Exception e) {
            System.out.println(e.toString());
        }
        //e.printStackTrace();
        //}

    }

    private static void printMenu() {
        System.out.println("Menu:" + System.lineSeparator() +
                "1." + UPDATE_USER_NAME + System.lineSeparator() +
                "2." + LOAD_REPOSITORY_FROM_XML + System.lineSeparator() +
                "3." + SWITCH_REPOSITORY + System.lineSeparator() +
                "4." + PRESENT_CURRENT_COMMIT_AND_HISTORY + System.lineSeparator() +
                "5." + SHOW_WORKING_COPY_STATUS + System.lineSeparator() +
                "6." + Commit + System.lineSeparator() +
                "7." + PRESENT_ALL_BRANCHES + System.lineSeparator() +
                "8." + CREATE_NEW_REPOSITORY + System.lineSeparator() +
                "9." + CREATE_NEW_BRANCH + System.lineSeparator() +
                "10." + DELETE_BRANCH + System.lineSeparator() +
                "11." + PICK_HEAD_BRANCH + System.lineSeparator() +
                "12." + PRESENT_CURRENT_BRANCH_HISTORY + System.lineSeparator() +
                "13." + CHANGE_HEAD_BRANCH_POINTED_COMMIT + System.lineSeparator() +
                "14." + EXIT);
    }

    private static void run(MagitEngine magitEngine) throws
            IOException, RepositoryNotFoundException, ParseException, ActiveBranchDeletedExpcetion, JAXBException {
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
                        continue;
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
                    presentCurrentBranchHistory(magitEngine, input);
                    break;
                case ChangePointedCommitSha1:
                    changePointedCommitSha1(magitEngine, input);
                    break;
                case Exit:
                    optionsToActivate = MenuOptions.Exit;
                    break;
                case Default:
                    break;
                default:
                    System.out.println("Please choose a number in the above range");
                    break;
            }
        } while (!optionsToActivate.equals(MenuOptions.Exit));
    }

    private static void loadRepositoryFromXML(MagitEngine magitEngine, Scanner input) throws ParseException, JAXBException {
        System.out.println("Please enter xml file path:");
        try {
            magitEngine.loadRepositoryFromXML(input.nextLine());
        } catch (PreviousCommitsLimitexceededException | IOException | XmlFileException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void updateUserName(MagitEngine magitEngine, Scanner input) {
        System.out.println("Please enter user name:");
        magitEngine.updateUserName(input.nextLine());
    }

    private static void presentCurrentCommitAndHistory(MagitEngine magitEngine) throws ParseException {
        try {
            System.out.println(magitEngine.presentCurrentCommitAndHistory());
        } catch (CommitNotFoundException e) {
            System.out.println(e.toString());
        } catch (RepositoryNotFoundException | IOException | PreviousCommitsLimitexceededException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void switchRepository(MagitEngine magitEngine, Scanner input) throws ParseException {
        try {
            executeSwitchRepository(magitEngine, input);
        } catch (IllegalPathException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void showWorkingCopyStatus(MagitEngine magitEngine) throws ParseException {
        try {
            System.out.println(magitEngine.getWorkingCopyStatus());
        } catch (RepositoryNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (PreviousCommitsLimitexceededException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void commit(MagitEngine magitEngine, Scanner input) throws ParseException {
        try {
            System.out.println("Please enter your commit message ");
            magitEngine.commit(input.nextLine());
        } catch (WorkingCopyIsEmptyException e) {
            System.out.println(e.toString());
        } catch (WorkingCopyStatusNotChangedComparedToLastCommitException e) {
            System.out.println(e.toString());
        } catch (RepositoryNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (PreviousCommitsLimitexceededException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void presentAllBranches(MagitEngine magitEngine) throws ParseException {
        try {
            System.out.println(magitEngine.getBranchesInfo());
        } catch (RepositoryNotFoundException | IOException | PreviousCommitsLimitexceededException e) {
            System.out.println(e.getMessage());
        }

    }

    private static void createNewRepository(MagitEngine magitEngine, Scanner input) {
        System.out.println("Please enter path:");
        Path pathToRepository = Paths.get(input.nextLine());
        System.out.println("Please enter repository name");
        try {
            magitEngine.createNewRepository(pathToRepository, input.nextLine());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void createNewBranch(MagitEngine magitEngine, Scanner input) throws ParseException {
        System.out.println("Pick branch name:");
        try {
            String branchName = input.nextLine();
            while (!magitEngine.createNewBranch(branchName))
                System.out.println(String.format("Branch already exists, pick another.%s", System.lineSeparator()));
            if (yesNoQuestion("Would you like to perform checkout to this branch right now? press Y/y to perform checkout or press any other key to cancel",
                    input)) {
                changeBranchOperation(magitEngine, branchName, input);
            }
        } catch (RepositoryNotFoundException | IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void deleteBranch(MagitEngine magitEngine, Scanner input) {
        System.out.println("Enter branch name:");
        try {
            magitEngine.deleteBranch(input.nextLine());
        } catch (RepositoryNotFoundException | IOException | BranchNotFoundException | ActiveBranchDeletedExpcetion e) {
            System.out.println(e.getMessage());
        }
    }

    private static void presentCurrentBranchHistory(MagitEngine magitEngine, Scanner input) throws ParseException {
        try {
            System.out.println(magitEngine.presentCurrentBranch());
        } catch (RepositoryNotFoundException | IOException | PreviousCommitsLimitexceededException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void changePointedCommitSha1(MagitEngine magitEngine, Scanner input) throws ParseException {
        System.out.println("Please Enter Commit's Sha1 code:");
        String commitSha1Code = input.nextLine();
        try {
            magitEngine.workingCopyChangedComparedToCommit();
            System.out.println(magitEngine.changeBranchPointedCommit(commitSha1Code));
        } catch (PreviousCommitsLimitexceededException e) {

        } catch (UncommitedChangesException e) {
            if (yesNoQuestion("Are you sure you want to change pointed commit before commiting unsaved changes?, press y/Y to change pointed commit, any other key to abort.", input)) {
                try {
                    System.out.println(magitEngine.changeBranchPointedCommit(commitSha1Code));
                } catch (CommitNotFoundException | RepositoryNotFoundException ex) {
                    System.out.println(ex.getMessage());
                } catch (PreviousCommitsLimitexceededException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.getMessage();
                }
            }
        } catch (CommitNotFoundException | IOException | RepositoryNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void executeSwitchRepository(MagitEngine magitEngine, Scanner input) throws ParseException, IOException {
        System.out.println("Please enter repository path:" + System.lineSeparator());
        Path pathOfRepository = Paths.get("");
        try {
            pathOfRepository = Paths.get(input.nextLine());
            magitEngine.switchRepository(pathOfRepository.toString());
        } catch (RepositoryNotFoundException ex) {
            if (yesNoQuestion("Repository not found, would you like to create one? Press Y/y to create one, any other button to cancel operation.",
                    input)) {
                System.out.println("Please enter repository name");
                magitEngine.createNewRepository(pathOfRepository, input.nextLine());
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
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
        System.out.println("Please enter branch name:");
        String branchName = "";
        changeBranchOperation(magitEngine, input.nextLine(), input);
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
                    magitEngine.forcedChangeBranch(branchName);
                } catch (PreviousCommitsLimitexceededException ex) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (PreviousCommitsLimitexceededException e) {
            System.out.println(e.getMessage());
        }
    }

    private enum MenuOptions {
        UpdateUserName,
        LoadRepositoryFromXML,
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

        public static MenuOptions getEnumByInt(int index) {
            return index <= 0 || index > values().length - 1 ? Default : values()[index - 1];
        }
    }

}

