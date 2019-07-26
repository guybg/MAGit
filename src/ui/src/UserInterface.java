import com.magit.logic.exceptions.IllegalPathException;
import com.magit.logic.exceptions.RepositoryAlreadyExistsException;
import com.magit.logic.exceptions.RepositoryNotFoundException;
import com.magit.logic.exceptions.WorkingCopyIsEmptyException;
import com.magit.logic.system.MagitEngine;

import java.io.IOException;
import java.sql.SQLOutput;
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
    private final static String CreateNewBranch = "Create New Branch";
    private final static String DeleteBranch = "Delete Branch";
    private final static String PickHeadBranch = "Checkout";
    private final static String PresentCurrentBranchHisoty = "Present Current Branch History";
    private final static String Exit = "Exit";

    public static void main(String[] args) throws IOException, ParseException {
        MagitEngine maGitSystem = new MagitEngine();
        try {
            maGitSystem.createNewRepository("testRep", "D:\\testingRep");
            run(maGitSystem);
            maGitSystem.commit();
        } catch (RepositoryAlreadyExistsException e) {
            System.out.println(e.getMessage() + "\n" +
                    e.getCause());

        } catch (IllegalPathException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e);
        } //catch (RepositoryNotFoundException e) {
        catch (WorkingCopyIsEmptyException e) {
            e.printStackTrace();
        }
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
                "8." + CreateNewBranch + System.lineSeparator() +
                "9." + DeleteBranch + System.lineSeparator() +
                "10." + PickHeadBranch + System.lineSeparator() +
                "11." + PresentCurrentBranchHisoty + System.lineSeparator() +
                "12." + Exit + System.lineSeparator());
    }

    private static void run(MagitEngine magitEngine) throws IOException, RepositoryNotFoundException, ParseException {
        Scanner input = new Scanner(System.in);
        printMenu();
        MenuOptions optionsToActivate = MenuOptions.Default;
        do {
            try {
                optionsToActivate = MenuOptions.getEnumByInt(input.nextInt());
                input.nextLine();
            } catch (InputMismatchException ex) {
                input.nextLine();
                System.out.println("Please choose a number.");
                continue;
            }
            switch (optionsToActivate) {
                case UpdateUserName:
                    System.out.println("Please enter user name:" + System.lineSeparator());
                    magitEngine.updateUserName(input.nextLine());
                    break;
                case ReadRepositoryDetails:
                    System.out.println("Please enter repository path:" + System.lineSeparator());
                    break;
                case SwitchRepository:
                    System.out.println("Please enter repository path:" + System.lineSeparator());
                    magitEngine.switchRepository(input.nextLine());
                    break;
                case PresentCurrentCommitAndHistory:
                    System.out.println(magitEngine.presentCurrentCommitAndHistory());
                    break;
                case ShowWorkingCopyStatus:

                    break;
                case Commit:

                    break;

                case PresentAllBranches:
                    String branchesInfo = magitEngine.getBranchesInfo();
                    System.out.println(branchesInfo);
                    break;
                case CreateNewBranch:
                    System.out.println(String.format("Pick branch name:%s", System.lineSeparator()));
                    while (!magitEngine.createNewBranch(input.nextLine()))
                        System.out.println(String.format("Branch already exists, pick another.%s", System.lineSeparator()));

                    break;
                case DeleteBranch:

                    break;
                case PickHeadBranch:

                    break;

                case PresentCurrentBranchHisoty:

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


    private enum MenuOptions {
        UpdateUserName,
        ReadRepositoryDetails,
        SwitchRepository,
        PresentCurrentCommitAndHistory,
        ShowWorkingCopyStatus,
        Commit,
        PresentAllBranches,
        CreateNewBranch,
        DeleteBranch,
        PickHeadBranch,
        PresentCurrentBranchHisoty,
        Exit,
        Default;

        public static MenuOptions getEnumByInt(int index) {
            return index <= 0 || index > values().length - 1 ? Default : values()[index - 1];
        }
    }

}

