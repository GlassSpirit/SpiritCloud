package ru.glassspirit.cloud.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.glassspirit.cloud.dao.FilesDao;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class FilesServiceImpl implements FilesService {

    @Autowired
    FilesDao filesDao;

    /**
     * Ищет и отдает файл относительно корневой папки пользователя.
     * Если ссылка указывает на файл выше корневой папки, отдает коневую папку пользователя (для безопасности).
     */
    @Override
    public File getFile(String path) {
        Path realPath = getUserDirectory().resolve(path).normalize();
        if (!realPath.startsWith(getUserDirectory())) {
            return getUserDirectory().toFile();
        }
        return filesDao.getFile(realPath.toString());
    }

    /**
     * Получает {@link OutputStream} для указанного в пути файла относительно корневой папки пользователя.
     */
    @Override
    public OutputStream getFileOutputStream(String path) throws IOException {
        return new FileOutputStream(getFile(path));
    }

    /**
     * Получает {@link InputStream} для указанного в пути файла относительно корневой папки пользователя.
     */
    @Override
    public InputStream getFileInputStream(String path) throws FileNotFoundException {
        return new FileInputStream(getFile(path));
    }

    /**
     * Получает список файлов в указанной по пути директории относительно корневой папки пользователя.
     * Если указана не директория, возвращает пустой список.
     */
    @Override
    public List<File> getFilesInDirectory(String path) {
        File directory = getFile(path);
        if (directory.exists() && directory.isDirectory())
            return Arrays.asList(directory.listFiles());
        return new ArrayList<>();
    }

    /**
     * Генерация частичной ссылки на загрузку файла.
     */
    @Override
    public String createDownloadURL(File file) {
        String url = "/download?file_path="
                + getUserDirectory().relativize(file.toPath()).toString().replace(File.separator, "/");
        return url;
    }

    /**
     * Получает корневую папку текущего пользователя.
     */
    private Path getUserDirectory() {
        Path userPath = filesDao.getRootPath().resolve(SecurityContextHolder.getContext().getAuthentication().getName());
        if (!userPath.toFile().exists())
            userPath.toFile().mkdirs();
        return userPath;
    }

}
