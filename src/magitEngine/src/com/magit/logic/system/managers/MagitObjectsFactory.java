package com.magit.logic.system.managers;

import com.magit.logic.system.XMLObjects.MagitCommits;
import com.magit.logic.system.XMLObjects.MagitSingleCommit;
import com.magit.logic.system.objects.Commit;

public class MagitObjectsFactory {

    public static MagitSingleCommit createMagitSingleCommit(Commit commit, Integer id) {
        MagitSingleCommit magitSingleCommit = new MagitSingleCommit();
        magitSingleCommit.setAuthor(commit.getmLastUpdater());
        magitSingleCommit.setDateOfCreation(commit.getCreationDate().toString());
        magitSingleCommit.setMessage(commit.getCommitMessage());
        magitSingleCommit.setId(id.toString());

        return magitSingleCommit;
    }
}
