package com.magit.webLogic.utils;

import com.magit.logic.exceptions.PreviousCommitsLimitExceededException;
import com.magit.logic.system.MagitEngine;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;

public class RepositoryUtils {
    public synchronized static HashMap<String, String> setRepositoryDetailsMap(String repositoryName, String commitDate, String commitMessage,String remoteRepositoryId,String remoteUserName, MagitEngine engine) throws IOException, ParseException, PreviousCommitsLimitExceededException {
        HashMap<String,String> repositoryDetails = new HashMap<>();
        String numberOfBranches = Integer.toString(engine.getmRepositoryManager().getBranches().size());
        repositoryDetails.put("name",repositoryName);
        repositoryDetails.put("activeBranch", engine.getmRepositoryManager().getHeadBranch());
        repositoryDetails.put("branchesNum", numberOfBranches);
        if(engine.getLastCommitDateAndMessage() != null){
            commitDate = engine.getLastCommitDateAndMessage().get(0);
            commitMessage = engine.getLastCommitDateAndMessage().get(1);
        }
        repositoryDetails.put("commitDate", commitDate);
        repositoryDetails.put("commitMessage", commitMessage);
        repositoryDetails.put("remote-id", remoteRepositoryId);
        repositoryDetails.put("remote-user", remoteUserName);
        return repositoryDetails;
    }
}
