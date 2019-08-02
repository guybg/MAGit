package com.magit.logic.system.managers;

import com.magit.logic.system.XMLObjects.*;
import com.magit.logic.system.objects.Blob;
import com.magit.logic.system.objects.Commit;
import com.magit.logic.system.objects.Tree;

class MagitObjectsFactory {

    static MagitSingleCommit createMagitSingleCommit(Commit commit, Integer id) {
        MagitSingleCommit magitSingleCommit = new MagitSingleCommit();
        magitSingleCommit.setAuthor(commit.getmLastUpdater());
        magitSingleCommit.setDateOfCreation(commit.getCreationDate().toString());
        magitSingleCommit.setMessage(commit.getCommitMessage());
        magitSingleCommit.setId(id.toString());

        return magitSingleCommit;
    }

    static MagitBlob createMagitBlob(Blob blob, Integer id) {
        MagitBlob magitBlob = new MagitBlob();
        magitBlob.setContent(blob.getFileContent());
        magitBlob.setId(id.toString());
        magitBlob.setLastUpdateDate(blob.getLastModified().toString());
        magitBlob.setLastUpdater(blob.getmLastUpdater());
        magitBlob.setName(blob.getmName());

        return magitBlob;
    }

    static MagitSingleFolder createMagitSingleFolder(Tree tree, Integer id, boolean isRoot) {
        MagitSingleFolder magitSingleFolder = new MagitSingleFolder();
        magitSingleFolder.setId(id.toString());
        magitSingleFolder.setIsRoot(isRoot);
        magitSingleFolder.setLastUpdateDate(tree.getLastModified().toString());
        magitSingleFolder.setLastUpdater(tree.getmLastUpdater());
        magitSingleFolder.setName(tree.getmName());

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
