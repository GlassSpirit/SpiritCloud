package ru.glassspirit.cloud.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class FilesServiceImpl implements FilesService {

    @Value("${filePath}")
    private String filePathValue;

    @Override
    public Path getRootPath() {
        try {
            return Paths.get(filePathValue).toRealPath();
        } catch (IOException e) {
            return Paths.get(filePathValue);
        }
    }

    /**
     * Ищет и отдает файл относительно корневой папки приложения.
     * Если ссылка указывает на файл выше корневого пути, отдает коневую папку (для безопасности).
     *
     * @param path Путь к файлу относительно корневого пути
     * @return Указанный файл (может не сущестовать).
     */
    @Override
    public File getFile(String path) {
        Path realPath = getRootPath().resolve(path).normalize();
        if (!realPath.startsWith(getRootPath()))
            return getRootPath().toFile();
        return getRootPath().resolve(path).toFile();
    }

    @Override
    public OutputStream getFileOutputStream(String path) throws IOException {
        return new FileOutputStream(getFile(path));
    }

    @Override
    public InputStream getFileInputStream(String path) throws FileNotFoundException {
        return new FileInputStream(getFile(path));
    }

    @Override
    public List<File> getFilesInDirectory(String path) {
        File directory = getFile(path);
        if (directory.exists() && directory.isDirectory())
            return Arrays.asList(directory.listFiles());
        return new ArrayList<>();
    }

}
