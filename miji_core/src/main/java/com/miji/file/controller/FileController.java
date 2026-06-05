package com.miji.file.controller;

import com.common.result.Result;
import com.miji.file.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping(value = "/upload")
    public Result upload(@RequestParam("file") MultipartFile file,
                         @RequestParam("type") String type) {
        return fileService.upload(file, type);
    }
}
