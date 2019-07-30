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
    private final static String READ_REPOSITORY_DETAILS = "Read Repository Details";
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
    private final static String Exit = "Exit";

    public static void main(String[] args) throws IOException, ParseException {
        MagitEngine maGitSystem = new MagitEngine();
        try {
            //maGitSystem.createNewRepository("testRep", "D:\\testingRep");
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
                "2." + READ_REPOSITORY_DETAILS + System.lineSeparator() +
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
                "13." + Exit);
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
                    System.out.println("Please enter user name:");
                    magitEngine.updateUserName(input.nextLine());
                    break;
                case ReadRepositoryDetails:
                    System.out.println("Please enter xml file path:");
                    magitEngine.readRepositoryDetailsFromXML(input.nextLine());
                    break;
                case SwitchRepository:
                    try {
                        switchRepository(magitEngine, input);
                    } catch (IllegalPathException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case PresentCurrentCommitAndHistory:
                    try {
                        System.out.println(magitEngine.presentCurrentCommitAndHistory());
                    } catch (CommitNotFoundException e) {
                        System.out.println(e.toString());
                    } catch (RepositoryNotFoundException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case ShowWorkingCopyStatus:
                    try {
                        System.out.println(magitEngine.getWorkingCopyStatus());
                    } catch (RepositoryNotFoundException e) {
                        System.out.println(e.getMessage());
                    }

                    break;
                case Commit:
                    try {
                        System.out.println("Please enter your commit message ");
                        magitEngine.commit(input.nextLine());
                    } catch (WorkingCopyIsEmptyException e) {
                        System.out.println(e.toString());
                    } catch (WorkingCopyStatusNotChangedComparedToLastCommitException e) {
                        System.out.println(e.toString());
                    } catch (RepositoryNotFoundException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case PresentAllBranches:
                    try {
                        System.out.println(magitEngine.getBranchesInfo());
                    } catch (RepositoryNotFoundException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case CreateNewRepository:
                    System.out.println("Please enter path:");
                    try {
                        Path pathToRepository = Paths.get(input.nextLine());
                        magitEngine.createNewRepository(pathToRepository);
                    } catch (IllegalPathException e) {
                        System.out.println(e.getMessage());
                        continue;
                    }
                case CreateNewBranch:
                    System.out.println("Pick branch name:");
                    try {
                        while (!magitEngine.createNewBranch(input.nextLine()))
                            System.out.println(String.format("Branch already exists, pick another.%s", System.lineSeparator()));
                    } catch (RepositoryNotFoundException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case DeleteBranch:
                    System.out.println("Enter branch name:");
                    try {
                        magitEngine.deleteBranch(input.nextLine());
                    } catch (RepositoryNotFoundException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case PickHeadBranch:
                    System.out.println("Please enter branch name:");
                    try {
                        System.out.println(magitEngine.pickHeadBranch(input.nextLine()));
                    } catch (RepositoryNotFoundException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case PresentCurrentBranchHistory:
                    try {
                        System.out.println(magitEngine.presentCurrentBranch());
                    } catch (RepositoryNotFoundException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case Exit:
                    optionsToActivate = MenuOptions.Exit;
                    break;
                default:
                    System.out.println("Please choose a number in the above range");
                    break;
            }
        } while (!optionsToActivate.equals(MenuOptions.Exit));
    }


    private static void switchRepository(MagitEngine magitEngine, Scanner input) throws IOException, ParseException {
        System.out.println("Please enter repository path:" + System.lineSeparator());
        Path pathOfRepository = Paths.get("");
        try {
            pathOfRepository = Paths.get(input.nextLine());
            magitEngine.switchRepository(pathOfRepository.toString());
        } catch (RepositoryNotFoundException ex) {
            System.out.println("Repository not found, would you like to create one? Press Y/y to create one, any other button to cancel operation.");
            String answer = input.nextLine();
            if (answer.equals("Y") ||answer.equals("y")) {
                magitEngine.createNewRepository(pathOfRepository);
            }
        }

    }

    private enum MenuOptions {
        UpdateUserName,
        ReadRepositoryDetails,
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
        Exit,
        Default;

        public static MenuOptions getEnumByInt(int index) {
            return index <= 0 || index > values().length - 1 ? Default : values()[index - 1];
        }
    }

}

