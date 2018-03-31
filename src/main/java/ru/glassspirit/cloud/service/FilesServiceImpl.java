package ru.glassspirit.cloud.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class FilesServiceImpl implements FilesService {

    @Value("${filePath}")
    private String filePath;

    @Override
    public Path getRootPath() {
        return new File(filePath).toPath();
    }

    @Override
    public File getFile(String path) {
        return getRootPath().resolve(path).toFile();
    }

    @Override
    public OutputStream getFileOutputStream(Path path) throws IOException {
        return new FileOutputStream(path.toFile());
    }

    @Override
    public InputStream getFileInputStream(Path path) throws FileNotFoundException {
        return new FileInputStream(path.toFile());
    }

    @Override
    public List<File> getFilesInDirectory(Path path) {
        File directory = path.toFile();
        if (directory.exists() && directory.isDirectory())
            return Arrays.asList(directory.listFiles());
        return new ArrayList<>();
    }

}
