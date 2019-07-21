import com.magit.logic.exceptions.IllegalPathException;
import com.magit.logic.exceptions.RepositoryAlreadyExistsException;
import com.magit.logic.system.MagitEngine;

import java.io.IOException;

public class UserInterface {
    public static void main(String[] args) {
        MagitEngine maGitSystem = new MagitEngine();
        try {
            maGitSystem.createNewRepository("testRep2e3", "c:/testingRep");

        } catch (RepositoryAlreadyExistsException e) {
            System.out.println(e.getMessage() + "\n" +
                    e.getCause());

        } catch (IllegalPathException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {

        }
    }
}

