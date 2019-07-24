import com.magit.logic.enums.FileType;
import com.magit.logic.exceptions.IllegalPathException;
import com.magit.logic.exceptions.RepositoryAlreadyExistsException;
import com.magit.logic.exceptions.RepositoryNotFoundException;
import com.magit.logic.system.MagitEngine;
import com.magit.logic.system.objects.Blob;

import java.io.IOException;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.Scanner;

public class UserInterface {

        final static String UpdateUserName = "Update User Name";
        final static String ReadRepositoryDetails = "Read Repository Details";
        final static String SwitchRepository = "Switch Repository";
        final static String PresentCurrentCommitAndHistory = "Present Current Commit and History";
        final static String ShowWorkingCopyStatus = "Show Working Copy Status";
        final static String Commit = "Commit";
        final static String PresentAllBranches = "Present All Branches";
        final static String CreateNewBranch = "Create New Branch";
        final static String DeleteBranch = "Delete Branch";
        final static String PickHeadBranch = "Checkout";
        final static String PresentCurrentBranchHisoty = "Present Current Branch History";
        final static String Exit = "Exit";

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

    public static void main(String[] args) {
        MagitEngine maGitSystem = new MagitEngine();
        try {
            run(maGitSystem);
            Blob blob = new Blob("test", "test", FileType.FILE, "ME", new Date());
            //FileZipper.unzip("C:\\testZip", new Sha1("test"), "C:\\testZip\\here", "banana.txt");
            maGitSystem.createNewRepository("testRep7", "C:\\testingRep");
            maGitSystem.commit();
        } catch (RepositoryAlreadyExistsException e) {
            System.out.println(e.getMessage() + "\n" +
                    e.getCause());

        } catch (IllegalPathException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println("oya");
        } catch (RepositoryNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static void printMenu() {
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


    public static void run(MagitEngine magitEngine) throws IOException, RepositoryNotFoundException {
        Scanner input = new Scanner(System.in);
        printMenu();
        MenuOptions optionsToActivate = MenuOptions.Default;
        do {
            try {
                optionsToActivate = MenuOptions.getEnumByInt(input.nextInt());
            } catch (InputMismatchException ex) {
                input.nextLine();
                System.out.println("Please choose a number.");
                continue;
            }
            switch (optionsToActivate) {
                case UpdateUserName:
                    System.out.println("Please enter user name:" + System.lineSeparator());
                    magitEngine.updateUserName(input.next());
                    break;
                case ReadRepositoryDetails:
                    System.out.println("Please enter repository path:" + System.lineSeparator());
                    break;
                case SwitchRepository:
                    System.out.println("Please enter repository path:" + System.lineSeparator());
                    magitEngine.switchRepository(input.next());
                    break;

                case PresentCurrentCommitAndHistory:

                    break;
                case ShowWorkingCopyStatus:

                    break;
                case Commit:

                    break;

                case PresentAllBranches:
                    break;

                case CreateNewBranch:

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

}

