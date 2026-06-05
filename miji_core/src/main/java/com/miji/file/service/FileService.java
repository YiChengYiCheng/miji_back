package com.miji.file.service;

import com.common.result.Result;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    Result upload(MultipartFile file, String type);
}
