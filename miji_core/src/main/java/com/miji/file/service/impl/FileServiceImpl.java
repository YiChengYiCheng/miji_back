package com.miji.file.service.impl;

import com.common.enums.CodeEnum;
import com.common.result.Result;
import com.miji.file.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(
            Arrays.asList("jpg", "jpeg", "png", "gif", "webp", "bmp")
    );
    private static final Set<String> ALLOWED_TYPES = new HashSet<>(
            Arrays.asList("avatar", "note")
    );

    @Value("${miji.upload.path}")
    private String uploadPath;

    @Value("${miji.upload.url-prefix}")
    private String urlPrefix;

    @Override
    public Result upload(MultipartFile file, String type) {
        if (file == null || file.isEmpty()) {
            return Result.fail(CodeEnum.PARAM_ERROR_NULL_VALUE.getStatusCode(), "file cannot be empty");
        }
        if (type == null || !ALLOWED_TYPES.contains(type)) {
            return Result.fail(CodeEnum.PARAM_ERROR_INVALID_VALUE.getStatusCode(), "unsupported upload type");
        }

        String extension = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return Result.fail(CodeEnum.PARAM_ERROR_INVALID_VALUE.getStatusCode(), "unsupported image type");
        }

        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path directory = Paths.get(uploadPath, type);

        Path target = directory.resolve(fileName).normalize();

        try {
            Files.createDirectories(directory);
            file.transferTo(target);
        } catch (IOException e) {
            return Result.fail(CodeEnum.COMMON_ERROR.getStatusCode(), "file upload fail");
        }

        String relativePath = type + "/" + fileName;
        String url = trimEndSlash(urlPrefix) + "/" + relativePath;

        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        data.put("path", relativePath);
        data.put("fileName", fileName);
        data.put("type", type);
        return Result.success(data);
    }

    private String getExtension(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int index = originalFilename.lastIndexOf(".");
        if (index < 0 || index == originalFilename.length() - 1) {
            return "";
        }
        return originalFilename.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private String trimEndSlash(String value) {
        if (value == null) {
            return "";
        }
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}
