package com.magit.logic.utils.compare;

import com.magit.logic.enums.FileStatus;
import com.magit.logic.enums.FileType;
import com.magit.logic.system.interfaces.DeltaAction;
import com.magit.logic.system.objects.FileItem;
import javafx.util.Pair;
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
                        && deltaItem.mFileItem.getFileType().equals(FileType.FILE))
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

    private static SortedSet<DeltaFileItem> getNewFiles(SortedSet<DeltaFileItem> firstItems,
                                                        SortedSet<DeltaFileItem> secondItems) {
        return getNewOrEditedFiles(firstItems, secondItems)
                .stream()
                .filter(item -> secondItems
                        .stream()
                        .map(DeltaFileItem::getFullPath)
                        .noneMatch(secondItem -> item.getFullPath().equals(secondItem))
                        && item.mFileItem.getFileType().equals(FileType.FILE)).distinct().collect(Collectors.toCollection(TreeSet::new));
    }

    private static SortedSet<DeltaFileItem> getEditedFiles(SortedSet<DeltaFileItem> firstItems,
                                                           SortedSet<DeltaFileItem> secondItems) {
        return new TreeSet<>(
                CollectionUtils
                        .intersection(getNewOrEditedFiles(firstItems, secondItems)
                                .stream()
                                .filter(item -> item.mFileItem.getFileType().equals(FileType.FILE))
                                .collect(Collectors.toSet()), secondItems));
    }

    private static SortedSet<DeltaFileItem> getDeletedFiles(SortedSet<DeltaFileItem> firstItems,
                                                            SortedSet<DeltaFileItem> secondItems) {
        return new TreeSet<>(
                CollectionUtils.subtract(secondItems
                        .stream()
                        .filter(item -> item.mFileItem.getFileType().equals(FileType.FILE))
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

    public static Map<String, Pair<FileStatus, DeltaFileItem>> getDiffrencesByPath( SortedSet<DeltaFileItem> firstItems,
             SortedSet<DeltaFileItem> secondItems) {
        Map<String, Pair<FileStatus, DeltaFileItem>> output = new TreeMap<>();

        Map<FileStatus, SortedSet<DeltaFileItem>> getDifferencesMap = getDifferences(firstItems, secondItems);

        for (Map.Entry<FileStatus, SortedSet<DeltaFileItem>> keyvalue : getDifferencesMap.entrySet()) {
            for (DeltaFileItem item : keyvalue.getValue()) {
                output.put(item.getFullPath(), new Pair<>(keyvalue.getKey(), item));
            }
        }
        return output;
    }

    public static class DeltaFileItem implements Comparable<DeltaFileItem>,
            Comparator<DeltaFileItem> {
        private final FileItem mFileItem;
        private final String mFilePath;

        public DeltaFileItem(FileItem fileItem, String mFilePath) {
            this.mFileItem = fileItem;
            this.mFilePath = Paths.get(mFilePath).toAbsolutePath().toString().toLowerCase();
        }

        public FileItem getFileItem() {return mFileItem;}

        public String getFullPath() {
            return mFilePath;
        }

        public String getFileName() { return mFileItem.getName();}
        public String getLastModified() { return mFileItem.getLastModified().toString();}
        public String getLastUpdater() { return  mFileItem.getLastUpdater();}

        String getPathAndSha1() {
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
