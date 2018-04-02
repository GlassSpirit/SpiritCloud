package ru.glassspirit.cloud.controller;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.glassspirit.cloud.service.FilesService;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;

@RestController
public class FileController {

    @Autowired
    FilesService filesService;

    @RequestMapping(
            value = "/download",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public void downloadFile(@RequestParam("file_path") String filePath, HttpServletResponse response) {
        File file = filesService.getFile(filePath);
        if (file.exists() && file.isFile()) {
            try {
                response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''"
                        + URLEncoder.encode(file.getName(), "UTF-8").replace("+", " "));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try {
                InputStream is = filesService.getFileInputStream(filePath);
                IOUtils.copy(is, response.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
