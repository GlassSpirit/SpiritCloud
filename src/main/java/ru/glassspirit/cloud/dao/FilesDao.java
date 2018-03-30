package ru.glassspirit.cloud.dao;

import java.io.IOException;
import java.io.OutputStream;

public interface FilesDao {

    OutputStream getFileOutputStream(String fileName) throws IOException;

}
