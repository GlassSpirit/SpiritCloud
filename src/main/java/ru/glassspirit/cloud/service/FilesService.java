package ru.glassspirit.cloud.service;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

public interface FilesService {

    Path getRootPath();

    File getFile(String path);

    List<File> getFilesInDirectory(Path path);

    OutputStream getFileOutputStream(Path path) throws IOException;

    InputStream getFileInputStream(Path path) throws FileNotFoundException;

}
