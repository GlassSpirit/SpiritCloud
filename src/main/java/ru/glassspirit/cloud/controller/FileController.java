package ru.glassspirit.cloud.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.glassspirit.cloud.service.FilesService;

import javax.servlet.http.HttpServletResponse;
import java.io.File;

@RestController
public class FileController {

    @Autowired
    FilesService filesService;

    @RequestMapping(
            value = "/files/**/{file_name}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public FileSystemResource getFile(@PathVariable("file_name") String fileName, HttpServletResponse response) {
        File file = filesService.getFile(fileName);
        if (file.exists()) {
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            return new FileSystemResource(file);
        } else return null;
    }

}
