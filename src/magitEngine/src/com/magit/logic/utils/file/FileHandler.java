package com.magit.logic.utils.file;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class FileHandler {
    String mFilePath;
    String mFileName;
    String mFileType;

    public static void clearFolder(Path repositoryPath) throws IOException {
        File directory = new File(repositoryPath.toString());
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File fileToDelete : files) {
            if (!fileToDelete.getName().equals(".magit")) {

                if (fileToDelete.isDirectory())
                    FileUtils.deleteDirectory(fileToDelete);
                else
                    FileUtils.deleteQuietly(fileToDelete);
            }

        }
    }
}
