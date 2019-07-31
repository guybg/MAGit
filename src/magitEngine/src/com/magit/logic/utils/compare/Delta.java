package com.magit.logic.utils.compare;

import com.magit.logic.enums.FileStatus;
import com.magit.logic.enums.FileType;
import com.magit.logic.system.interfaces.DeltaAction;
import com.magit.logic.system.objects.FileItem;
import org.apache.commons.collections4.CollectionUtils;

import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Delta {
    public static SortedSet<DeltaFileItem> getUnchangedFiles(SortedSet<DeltaFileItem> firstItems,
                                                             SortedSet<DeltaFileItem> secondItems) {
        DeltaAction<SortedSet<DeltaFileItem>> deltaAction = (firstItem, secondItem) -> new TreeSet<>(firstItem.stream()
                .filter(deltaItem -> secondItem
                        .stream()
                        .map(DeltaFileItem::getPathAndSha1)
                        .anyMatch(secondPathAndSha1 -> secondPathAndSha1.equals(deltaItem.getPathAndSha1()))
                        && deltaItem.mFileItem.getmFileType().equals(FileType.FILE))
                .collect(Collectors.toSet()));
        return deltaAction.execute(firstItems, secondItems);
    }

    private static SortedSet<DeltaFileItem> getNewOrEditedFiles(SortedSet<DeltaFileItem> firstItems,
                                                                SortedSet<DeltaFileItem> secondItems) {
        DeltaAction<SortedSet<DeltaFileItem>> deltaAction = (firstItem, secondItem) -> new TreeSet<>(firstItem.stream()
                .filter(deltaItem -> secondItem
                        .stream()
                        .map(DeltaFileItem::getPathAndSha1)
                        .noneMatch(secondPathAndSha1 -> secondPathAndSha1.equals(deltaItem.getPathAndSha1())))
                .collect(Collectors.toSet()));
        return deltaAction.execute(firstItems, secondItems);
    }

    public static SortedSet<DeltaFileItem> getNewFiles(SortedSet<DeltaFileItem> firstItems,
                                                       SortedSet<DeltaFileItem> secondItems) {
        return new TreeSet<>(getNewOrEditedFiles(firstItems, secondItems)
                .stream()
                .filter(item -> secondItems
                        .stream()
                        .map(DeltaFileItem::getFullPath)
                        .noneMatch(secondItem -> item.getFullPath().equals(secondItem))
                        && item.mFileItem.getmFileType().equals(FileType.FILE))
                .collect(Collectors.toSet()));
    }

    public static SortedSet<DeltaFileItem> getEditedFiles(SortedSet<DeltaFileItem> firstItems,
                                                          SortedSet<DeltaFileItem> secondItems) {
        return new TreeSet<>(
                CollectionUtils
                        .intersection(getNewOrEditedFiles(firstItems, secondItems)
                .stream()
                                .filter(item -> item.mFileItem.getmFileType().equals(FileType.FILE))
                                .collect(Collectors.toSet()), secondItems));
    }

    public static SortedSet<DeltaFileItem> getDeletedFiles(SortedSet<DeltaFileItem> firstItems,
                                                           SortedSet<DeltaFileItem> secondItems) {
        return new TreeSet<>(
                CollectionUtils.subtract(secondItems
                        .stream()
                        .filter(item -> item.mFileItem.getmFileType().equals(FileType.FILE))
                        .collect(Collectors.toSet()), firstItems));
    }

    public static Map<FileStatus, SortedSet<DeltaFileItem>> getDifferences(SortedSet<DeltaFileItem> firstItems,
                                                                           SortedSet<DeltaFileItem> secondItems) {
        Map<FileStatus, SortedSet<DeltaFileItem>> differences = new TreeMap<>();
        differences.put(FileStatus.NEW, getNewFiles(firstItems, secondItems));
        differences.put(FileStatus.REMOVED, getDeletedFiles(firstItems, secondItems));
        differences.put(FileStatus.EDITED, getEditedFiles(firstItems, secondItems));
        return differences;
    }


    public static class DeltaFileItem implements Comparable<DeltaFileItem>,
            Comparator<DeltaFileItem> {
        private FileItem mFileItem;
        private String mFilePath;

        public DeltaFileItem(FileItem fileItem, String mFilePath) {
            this.mFileItem = fileItem;
            this.mFilePath = mFilePath;
        }

        public String getFullPath() {
            return Paths.get(mFilePath).toString();
        }

        public String getPathAndSha1() {
            return getFullPath() + ";" + mFileItem.getSha1Code();
        }

        @Override
        public int compareTo(DeltaFileItem o) {
            return mFilePath.compareTo(o.mFilePath);
        }

        @Override
        public int compare(DeltaFileItem o1, DeltaFileItem o2) {
            return o1.compareTo(o2);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DeltaFileItem)) return false;
            DeltaFileItem that = (DeltaFileItem) o;
            return Objects.equals(mFilePath, that.mFilePath);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mFilePath);
        }
    }


}
