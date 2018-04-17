package ru.glassspirit.cloud.dao;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

public interface FilesDao {

    Path getRootPath();

    File getFile(String path);

    OutputStream getFileOutputStream(String path) throws IOException;

    InputStream getFileInputStream(String path) throws FileNotFoundException;

    List<File> getFilesInDirectory(String path);

}
