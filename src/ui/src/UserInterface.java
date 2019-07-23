import com.magit.logic.enums.FileType;
import com.magit.logic.exceptions.IllegalPathException;
import com.magit.logic.exceptions.RepositoryAlreadyExistsException;
import com.magit.logic.system.MagitEngine;
import com.magit.logic.system.objects.Blob;

import java.io.IOException;
import java.util.Date;

public class UserInterface {
    public static void main(String[] args) {
        MagitEngine maGitSystem = new MagitEngine();
        try {
            Blob blob = new Blob("test", "test", FileType.FILE, "ME", new Date());
            //FileZipper.unzip("C:\\testZip", new Sha1("test"), "C:\\testZip\\here", "banana.txt");
            maGitSystem.createNewRepository("testRep7", "/Users/guybergman/Desktop/gitRep");
            maGitSystem.commit();
        } catch (RepositoryAlreadyExistsException e) {
            System.out.println(e.getMessage() + "\n" +
                    e.getCause());

        } catch (IllegalPathException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println("oya");
        }
    }
}

