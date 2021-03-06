package ru.glassspirit.cloud.service;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

public interface FilesService {

    File getFile(String path);

    List<File> getFilesInDirectory(String path);

    OutputStream getFileOutputStream(String path) throws IOException;

    InputStream getFileInputStream(String path) throws FileNotFoundException;

    String createDownloadURL(File file);
}
