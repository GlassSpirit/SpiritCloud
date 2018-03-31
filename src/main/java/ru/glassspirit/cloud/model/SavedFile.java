package ru.glassspirit.cloud.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SavedFile {

    private File file;

    public SavedFile(File file) {
        this.file = file;
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
        return file.delete();
    }

    public static List<SavedFile> fromFileList(List<File> list) {
        List<SavedFile> fileList = new ArrayList<>();
        for (File file : list) fileList.add(new SavedFile(file));
        return fileList;
    }
}
