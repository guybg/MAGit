package com.magit.logic.system.managers;

import com.magit.logic.system.XMLObjects.*;
import com.magit.logic.system.objects.Blob;
import com.magit.logic.system.objects.Commit;
import com.magit.logic.system.objects.Tree;

import java.text.SimpleDateFormat;

class MagitObjectsFactory {

    private static final String DATE_TIME_FORMAT = "dd.MM.yyyy-HH:mm:ss:SSS";
    static MagitSingleCommit createMagitSingleCommit(Commit commit, Integer id) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_FORMAT);
        MagitSingleCommit magitSingleCommit = new MagitSingleCommit();
        magitSingleCommit.setAuthor(commit.getLastUpdater());
        magitSingleCommit.setDateOfCreation(formatter.format(commit.getCreationDate()));
        magitSingleCommit.setMessage(commit.getCommitMessage());
        magitSingleCommit.setId(id.toString());

        return magitSingleCommit;
    }

    static MagitBlob createMagitBlob(Blob blob, Integer id) {
        MagitBlob magitBlob = new MagitBlob();
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_FORMAT);
        magitBlob.setContent(blob.getFileContent());
        magitBlob.setId(id.toString());
        magitBlob.setLastUpdateDate(formatter.format(blob.getLastModified()));
        magitBlob.setLastUpdater(blob.getLastUpdater());
        magitBlob.setName(blob.getName());

        return magitBlob;
    }

    static MagitSingleFolder createMagitSingleFolder(Tree tree, Integer id, boolean isRoot) {
        MagitSingleFolder magitSingleFolder = new MagitSingleFolder();
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_FORMAT);
        magitSingleFolder.setId(id.toString());
        magitSingleFolder.setIsRoot(isRoot);
        magitSingleFolder.setLastUpdateDate(formatter.format(tree.getLastModified()));
        magitSingleFolder.setLastUpdater(tree.getLastUpdater());
        magitSingleFolder.setName(tree.getName());

        return magitSingleFolder;
    }

    static Item createItem(Integer id, String type) {
        Item item = new Item();
        item.setId(id.toString());
        item.setType(type);

        return item;
    }

    static MagitSingleFolder.Items createItemList() {
        return new MagitSingleFolder.Items();
    }

    static MagitBlobs createMagitBlobs() {
        return new MagitBlobs();
    }

    static MagitFolders createMagitFolders() {
        return new MagitFolders();
    }

    static MagitCommits createMagitCommits() {
        return new MagitCommits();
    }
}
