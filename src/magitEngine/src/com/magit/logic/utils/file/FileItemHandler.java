package com.magit.logic.utils.file;

import com.magit.logic.system.objects.Blob;
import com.magit.logic.system.objects.Commit;
import com.magit.logic.system.objects.FileItem;
import com.magit.logic.system.objects.Tree;
import com.magit.logic.utils.digest.Sha1;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.zip.*;

public class FileItemHandler {


    public FileItemHandler() {
    }

    static void fileItemToFile(Blob fileItem, String destinationPath, String fileName) throws IOException {
        FileHandler.writeNewFile(Paths.get(destinationPath, fileName).toString(), fileItem.getFileContent());
    }

    static void fileItemToFile(Tree fileItem, String destinationPath, String fileName) {
        FileHandler.writeNewFolder(Paths.get(destinationPath, fileName).toString());
    }

}
