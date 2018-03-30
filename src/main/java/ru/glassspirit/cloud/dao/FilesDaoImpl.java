package ru.glassspirit.cloud.dao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@Repository
public class FilesDaoImpl implements FilesDao {

    @Value("${filePath}")
    private String filePath;

    @Override
    public OutputStream getFileOutputStream(String fileName) throws IOException {
        File file = new File(filePath + "\\" + fileName);
        return new FileOutputStream(file);
    }
}
