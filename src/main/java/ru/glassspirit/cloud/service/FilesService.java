package ru.glassspirit.cloud.service;

import java.io.InputStream;
import java.io.OutputStream;

public interface FilesService {

    OutputStream getFileOutputStream(String path);

    InputStream getFileInputStream(String path);

}
