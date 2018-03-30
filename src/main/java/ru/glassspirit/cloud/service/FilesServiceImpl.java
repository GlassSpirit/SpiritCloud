package ru.glassspirit.cloud.service;

import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;

@Service
public class FilesServiceImpl implements FilesService {

    @Override
    public OutputStream getFileOutputStream(String path) {
        return null;
    }

    @Override
    public InputStream getFileInputStream(String path) {
        return null;
    }

}
