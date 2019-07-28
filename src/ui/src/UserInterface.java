import com.magit.logic.exceptions.*;
import com.magit.logic.system.MagitEngine;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class UserInterface {

    private final static String UpdateUserName = "Update User Name";
    private final static String ReadRepositoryDetails = "Read Repository Details";
    private final static String SwitchRepository = "Switch Repository";
    private final static String PresentCurrentCommitAndHistory = "Present Current Commit and History";
    private final static String ShowWorkingCopyStatus = "Show Working Copy Status";
    private final static String Commit = "Commit";
    private final static String PresentAllBranches = "Present All Branches";
    private final static String CreateNewRepository = "Create New Repository";
    private final static String CreateNewBranch = "Create New Branch";
    private final static String DeleteBranch = "Delete Branch";
    private final static String PickHeadBranch = "Checkout";
    private final static String PresentCurrentBranchHisoty = "Present Current Branch History";
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
                "1." + UpdateUserName + System.lineSeparator() +
                "2." + ReadRepositoryDetails + System.lineSeparator() +
                "3." + SwitchRepository + System.lineSeparator() +
                "4." + PresentCurrentCommitAndHistory + System.lineSeparator() +
                "5." + ShowWorkingCopyStatus + System.lineSeparator() +
                "6." + Commit + System.lineSeparator() +
                "7." + PresentAllBranches + System.lineSeparator() +
                "8." + CreateNewRepository + System.lineSeparator() +
                "9." + CreateNewBranch + System.lineSeparator() +
                "10." + DeleteBranch + System.lineSeparator() +
                "11." + PickHeadBranch + System.lineSeparator() +
                "12." + PresentCurrentBranchHisoty + System.lineSeparator() +
                "13." + Exit + System.lineSeparator());
    }

    private static void run(MagitEngine magitEngine) throws
            IOException, RepositoryNotFoundException, ParseException, ActiveBranchDeletedExpcetion {
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
                    System.out.println("Please enter repository path:");
                    break;
                case SwitchRepository:
                    switchRepository(magitEngine, input);
                    break;
                case PresentCurrentCommitAndHistory:
                    System.out.println(magitEngine.presentCurrentCommitAndHistory());
                    break;
                case ShowWorkingCopyStatus:
                    magitEngine.checkDifferenceBetweenCurrentWCandLastCommit();
                    break;
                case Commit:
                    try {
                        System.out.println("Please enter your commit message ");
                        magitEngine.commit(input.nextLine());
                    } catch (WorkingCopyIsEmptyException e) {
                        System.out.println(e.toString());
                    } catch (WorkingCopyStatusNotChangedComparedToLastCommitException e) {
                        System.out.println(e.toString());
                    }
                    break;
                case PresentAllBranches:
                    System.out.println(magitEngine.getBranchesInfo());
                    break;
                case CreateNewRepository:
                    System.out.println("Please enter path:");
                    Path pathToRepository = Paths.get(input.nextLine());
                    magitEngine.createNewRepository(pathToRepository);
                case CreateNewBranch:
                    System.out.println("Pick branch name:");
                    while (!magitEngine.createNewBranch(input.nextLine()))
                        System.out.println(String.format("Branch already exists, pick another.%s", System.lineSeparator()));
                    break;
                case DeleteBranch:
                    System.out.println("Enter branch name:");
                    magitEngine.deleteBranch(input.nextLine());
                    break;
                case PickHeadBranch:
                    System.out.println("Please enter branch name:");
                    System.out.println(magitEngine.pickHeadBranch(input.nextLine()));
                    break;
                case PresentCurrentBranchHistory:
                    System.out.println(magitEngine.presentCurrentBranch());
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

