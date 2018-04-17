package ru.glassspirit.cloud.dao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Repository
public class FilesDaoImpl implements FilesDao {
    @Value("${rootPath}")
    private String rootPathValue;

    /**
     * Получает корневую папку для работы приложения, указанную в настройках.
     */
    @Override
    public Path getRootPath() {
        try {
            return Paths.get(rootPathValue).toRealPath();
        } catch (IOException e) {
            return Paths.get(rootPathValue);
        }
    }

    /**
     * Ищет и отдает файл относительно корневой папки.
     * Если ссылка указывает на файл выше корневого пути, отдает коневую папку (для безопасности).
     */
    @Override
    public File getFile(String path) {
        Path realPath = getRootPath().resolve(path).normalize();
        if (!realPath.startsWith(getRootPath()))
            return getRootPath().toFile();
        return getRootPath().resolve(path).toFile();
    }

    /**
     * Получает {@link OutputStream} для указанного в пути файла относительно корневой папки.
     */
    @Override
    public OutputStream getFileOutputStream(String path) throws IOException {
        return new FileOutputStream(getFile(path));
    }

    /**
     * Получает {@link InputStream} для указанного в пути файла относительно корневой папки.
     */
    @Override
    public InputStream getFileInputStream(String path) throws FileNotFoundException {
        return new FileInputStream(getFile(path));
    }

    /**
     * Получает список файлов в указанной по пути директории относительно корневой папки.
     * Если указана не директория, возвращает пустой список.
     */
    @Override
    public List<File> getFilesInDirectory(String path) {
        File directory = getFile(path);
        if (directory.exists() && directory.isDirectory())
            return Arrays.asList(directory.listFiles());
        return new ArrayList<>();
    }
}
