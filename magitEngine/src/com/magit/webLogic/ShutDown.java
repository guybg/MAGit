package com.magit.webLogic;

import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ShutDown {
    public static void onShutDown(){
        Path magitServerPath = Paths.get("magit-ex3");
        if(Files.exists(magitServerPath)){
            FileUtils.deleteQuietly(magitServerPath.toFile());
        }
    }
}
