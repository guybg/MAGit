package com.magit.logic.utils.file;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FileWriter {
    // private String mFilePath;
    // private String mFileContent;


    public static void writeNewFile(String destinationPath, String fileContent) throws IOException {
        File file = new File(destinationPath);
        FileUtils.writeStringToFile(file, fileContent, StandardCharsets.UTF_8);
    }
}
