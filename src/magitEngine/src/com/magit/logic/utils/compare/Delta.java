package com.magit.logic.utils.compare;

import com.magit.logic.system.interfaces.DeltaAction;
import com.magit.logic.system.objects.FileItem;
import org.apache.commons.collections4.CollectionUtils;

import java.nio.file.Paths;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class Delta {
    public static SortedSet<DeltaFileItem> getUnchangedItems(SortedSet<DeltaFileItem> firstItems, SortedSet<DeltaFileItem> secondItems) {
        DeltaAction<SortedSet<DeltaFileItem>> deltaAction = new DeltaAction<SortedSet<DeltaFileItem>>() {
            @Override
            public SortedSet<DeltaFileItem> execute(SortedSet<DeltaFileItem> firstItem, SortedSet<DeltaFileItem> secondItem) {
                return new TreeSet<>(firstItem.stream()
                        .filter(deltaItem -> secondItem
                                .stream()
                                .map(DeltaFileItem::getPathAndSha1)
                                .anyMatch(secondPathAndSha1 -> secondPathAndSha1.equals(deltaItem.getPathAndSha1())))
                        .collect(Collectors.toSet()));
            }
        };
        return deltaAction.execute(firstItems, secondItems);
    }

    private static SortedSet<DeltaFileItem> getNewOrEditedItems(SortedSet<DeltaFileItem> firstItems, SortedSet<DeltaFileItem> secondItems) {
        DeltaAction<SortedSet<DeltaFileItem>> deltaAction = new DeltaAction<SortedSet<DeltaFileItem>>() {
            @Override
            public SortedSet<DeltaFileItem> execute(SortedSet<DeltaFileItem> firstItem, SortedSet<DeltaFileItem> secondItem) {
                return new TreeSet<>(firstItem.stream()
                        .filter(deltaItem -> secondItem
                                .stream()
                                .map(DeltaFileItem::getPathAndSha1)
                                .noneMatch(secondPathAndSha1 -> secondPathAndSha1.equals(deltaItem.getPathAndSha1())))
                        .collect(Collectors.toSet()));
            }
        };
        return deltaAction.execute(firstItems, secondItems);
    }

    public static SortedSet<DeltaFileItem> getNewItems(SortedSet<DeltaFileItem> firstItems, SortedSet<DeltaFileItem> secondItems) {
        return new TreeSet<>(getNewOrEditedItems(firstItems, secondItems)
                .stream()
                .filter(item -> secondItems
                        .stream()
                        .map(DeltaFileItem::getFullPath)
                        .noneMatch(secondItem -> item.equals(secondItem))).collect(Collectors.toSet()));
    }

    public static SortedSet<DeltaFileItem> getEditedItems(SortedSet<DeltaFileItem> firstItems, SortedSet<DeltaFileItem> secondItems) {
        return new TreeSet<>(getNewOrEditedItems(firstItems, secondItems)
                .stream()
                .filter(item -> secondItems
                        .stream()
                        .map(DeltaFileItem::getFullPath)
                        .anyMatch(secondItem -> item.equals(secondItem))).collect(Collectors.toSet()));
    }

    public static SortedSet<DeltaFileItem> getDeletedItems(SortedSet<DeltaFileItem> firstItems, SortedSet<DeltaFileItem> secondItems) {
        return new TreeSet<>(CollectionUtils.subtract(secondItems, getNewOrEditedItems(firstItems, secondItems)));
    }


    public static class DeltaFileItem implements Comparable<DeltaFileItem> {
        private FileItem mFileItem;
        private String mFilePath;

        public DeltaFileItem(FileItem fileItem, String mFilePath) {
            this.mFileItem = fileItem;
            this.mFilePath = mFilePath;
        }

        public String getFullPath() {
            return Paths.get(mFilePath, mFileItem.getmName()).toString();
        }

        public String getPathAndSha1() {
            return getFullPath() + ";" + mFileItem.getSha1Code();
        }

        @Override
        public int compareTo(DeltaFileItem o) {
            return mFilePath.compareTo(o.mFilePath);
        }
    }


}
