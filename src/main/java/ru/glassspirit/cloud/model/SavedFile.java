package ru.glassspirit.cloud.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Spliterator;

public class SavedFile {

    private File file;

    public SavedFile(File file) {
        this.file = file;
    }

    public static List<SavedFile> fromFileList(List<File> list) {
        List<SavedFile> fileList = new ArrayList<>();
        for (File file : list) fileList.add(new SavedFile(file));
        return fileList;
    }

    public File getFile() {
        return file;
    }

    public String getFileName() {
        return file.getName();
    }

    public String getFileSize() {
        if (file.isDirectory()) return "";
        return file.length() + " bytes";
    }

    public boolean delete() {
        if (file.isDirectory())
            return deleteDirectory(file);
        return file.delete();
    }

    private boolean deleteDirectory(File file) {
        Spliterator<File> spliterator = Arrays.asList(file.listFiles()).spliterator();
        while (spliterator.tryAdvance(element -> {
            if (element.isDirectory()) deleteDirectory(element);
            else element.delete();
        })) ;
        return file.delete();
    }
}
