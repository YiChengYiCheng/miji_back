package com.miji.core.file.service.impl;

import com.common.enums.CodeEnum;
import com.common.result.Result;
import com.miji.core.file.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
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
    private static final Set<String> ALLOWED_CONTENT_TYPES = new HashSet<>(
            Arrays.asList("image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp")
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final int NOTE_MAX_SIZE = 960;
    private static final int AVATAR_MAX_SIZE = 360;
    private static final int MIN_DISPLAY_SIZE = 480;
    private static final long NOTE_TARGET_FILE_SIZE = 300 * 1024;
    private static final long AVATAR_TARGET_FILE_SIZE = 120 * 1024;
    private static final float JPEG_INITIAL_QUALITY = 0.68F;
    private static final float JPEG_MIN_QUALITY = 0.45F;
    private static final float JPEG_QUALITY_STEP = 0.08F;
    private static final String ORIGIN_DIRECTORY = "origin";
    private static final String DISPLAY_EXTENSION = "jpg";

    @Value("${miji.upload.path}")
    private String uploadPath;

    @Value("${miji.upload.url-prefix}")
    private String urlPrefix;

    @Override
    public Result upload(MultipartFile file, String type) {
        if (file == null || file.isEmpty()) {
            return Result.fail(CodeEnum.PARAM_ERROR_NULL_VALUE.getStatusCode(), "file cannot be empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.fail(CodeEnum.PARAM_ERROR_INVALID_VALUE.getStatusCode(), "file size cannot exceed 5MB");
        }
        if (type == null || !ALLOWED_TYPES.contains(type)) {
            return Result.fail(CodeEnum.PARAM_ERROR_INVALID_VALUE.getStatusCode(), "unsupported upload type");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            return Result.fail(CodeEnum.PARAM_ERROR_INVALID_VALUE.getStatusCode(), "unsupported image content type");
        }

        String extension = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return Result.fail(CodeEnum.PARAM_ERROR_INVALID_VALUE.getStatusCode(), "unsupported image type");
        }
        BufferedImage image = readImage(file);
        if (!"webp".equals(extension) && image == null) {
            return Result.fail(CodeEnum.PARAM_ERROR_INVALID_VALUE.getStatusCode(), "invalid image file");
        }

        String fileBaseName = UUID.randomUUID().toString().replace("-", "");
        String fileName = fileBaseName + "." + getDisplayExtension(extension, image);
        String originFileName = fileBaseName + "." + extension;
        Path root = Paths.get(uploadPath).toAbsolutePath().normalize();
        Path directory = root.resolve(type).normalize();
        Path originDirectory = directory.resolve(ORIGIN_DIRECTORY).normalize();

        Path target = directory.resolve(fileName).normalize();
        Path originTarget = originDirectory.resolve(originFileName).normalize();
        if (!target.startsWith(root) || !originTarget.startsWith(root)) {
            return Result.fail(CodeEnum.PARAM_ERROR_INVALID_VALUE.getStatusCode(), "invalid upload path");
        }

        try {
            Files.createDirectories(directory);
            Files.createDirectories(originDirectory);
            file.transferTo(originTarget);
            saveDisplayImage(image, extension, type, originTarget, target);
        } catch (IOException e) {
            log.info("file upload fail", e);
            return Result.fail(CodeEnum.COMMON_ERROR.getStatusCode(), "file upload fail");
        }

        String relativePath = type + "/" + fileName;
        String originRelativePath = type + "/" + ORIGIN_DIRECTORY + "/" + originFileName;
        String url = trimEndSlash(urlPrefix) + "/" + relativePath;
        String originUrl = trimEndSlash(urlPrefix) + "/" + originRelativePath;

        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        data.put("path", relativePath);
        data.put("originUrl", originUrl);
        data.put("originPath", originRelativePath);
        data.put("fileName", fileName);
        data.put("originFileName", originFileName);
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

    private BufferedImage readImage(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            return ImageIO.read(inputStream);
        } catch (IOException e) {
            log.info("image read fail", e);
            return null;
        }
    }

    private void saveDisplayImage(BufferedImage image, String extension, String type, Path originTarget, Path target)
            throws IOException {
        if (image == null || "gif".equals(extension) || "webp".equals(extension)) {
            Files.copy(originTarget, target, StandardCopyOption.REPLACE_EXISTING);
            return;
        }

        BufferedImage resizedImage = resizeIfNecessary(image, getMaxSize(type), true);
        writeJpegWithTargetSize(resizedImage, target, type);
    }

    private String getDisplayExtension(String extension, BufferedImage image) {
        if (image == null || "gif".equals(extension) || "webp".equals(extension)) {
            return extension;
        }
        return DISPLAY_EXTENSION;
    }

    private int getMaxSize(String type) {
        return "avatar".equals(type) ? AVATAR_MAX_SIZE : NOTE_MAX_SIZE;
    }

    private long getTargetFileSize(String type) {
        return "avatar".equals(type) ? AVATAR_TARGET_FILE_SIZE : NOTE_TARGET_FILE_SIZE;
    }

    private BufferedImage resizeIfNecessary(BufferedImage image, int maxSize, boolean rgb) {
        int width = image.getWidth();
        int height = image.getHeight();
        double scale = Math.min(1.0D, (double) maxSize / Math.max(width, height));
        int targetWidth = Math.max(1, (int) Math.round(width * scale));
        int targetHeight = Math.max(1, (int) Math.round(height * scale));
        int imageType = rgb ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;

        if (scale == 1.0D && image.getType() == imageType) {
            return image;
        }

        BufferedImage targetImage = new BufferedImage(targetWidth, targetHeight, imageType);
        Graphics2D graphics = targetImage.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (rgb) {
                graphics.setColor(java.awt.Color.WHITE);
                graphics.fillRect(0, 0, targetWidth, targetHeight);
            }
            graphics.drawImage(image, 0, 0, targetWidth, targetHeight, null);
        } finally {
            graphics.dispose();
        }
        return targetImage;
    }

    private void writeJpegWithTargetSize(BufferedImage image, Path target, String type) throws IOException {
        BufferedImage currentImage = image;
        float quality = JPEG_INITIAL_QUALITY;
        long targetFileSize = getTargetFileSize(type);

        for (int i = 0; i < 10; i++) {
            writeJpeg(currentImage, target, quality);
            if (Files.size(target) <= targetFileSize) {
                return;
            }

            if (quality > JPEG_MIN_QUALITY) {
                quality = Math.max(JPEG_MIN_QUALITY, quality - JPEG_QUALITY_STEP);
                continue;
            }

            int currentMaxSize = Math.max(currentImage.getWidth(), currentImage.getHeight());
            if (currentMaxSize <= MIN_DISPLAY_SIZE) {
                return;
            }
            int nextMaxSize = Math.max(MIN_DISPLAY_SIZE, (int) Math.round(currentMaxSize * 0.85D));
            currentImage = resizeIfNecessary(currentImage, nextMaxSize, true);
            quality = JPEG_INITIAL_QUALITY;
        }
    }

    private void writeJpeg(BufferedImage image, Path target, float quality) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            ImageIO.write(image, "jpg", target.toFile());
            return;
        }

        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
        }

        try (ImageOutputStream outputStream = ImageIO.createImageOutputStream(target.toFile())) {
            writer.setOutput(outputStream);
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
    }
}
