package ru.glassspirit.cloud.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import ru.glassspirit.cloud.service.FilesService;

@RestController
public class FileController {

    @Autowired
    FilesService filesService;

}
