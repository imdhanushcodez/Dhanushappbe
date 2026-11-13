package com.developersfab.Dhanushappbe.upload;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.developersfab.Dhanushappbe.common.AppException;
import com.developersfab.Dhanushappbe.common.ErrorCode;
import com.developersfab.Dhanushappbe.common.FieldErrorItem;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class ImageUploadService {

    private static final Set<String> ALLOWED_FOLDERS = Set.of("profile", "workout", "food");
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2 MB

    @Value("${app.uploads.cloudinary.cloud-name:}")
    private String cloudinaryCloudName;

    @Value("${app.uploads.cloudinary.api-key:}")
    private String cloudinaryApiKey;

    @Value("${app.uploads.cloudinary.api-secret:}")
    private String cloudinaryApiSecret;

    @Value("${app.uploads.local-dir:./uploads}")
    private String localUploadDir;

    @Value("${app.uploads.public-base-url:http://localhost:8080/api}")
    private String publicBaseUrl;

    private Cloudinary cloudinary;

    @PostConstruct
    public void init() {
        if (isCloudinaryConfigured()) {
            this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudinaryCloudName.trim(),
                    "api_key", cloudinaryApiKey.trim(),
                    "api_secret", cloudinaryApiSecret.trim(),
                    "secure", true
            ));
            log.info("Cloudinary storage initialized for cloud_name: {}", cloudinaryCloudName.trim());
        } else {
            log.info("Cloudinary credentials not configured; using local filesystem storage ({})", localUploadDir);
        }
    }

    public boolean isCloudinaryConfigured() {
        return cloudinaryCloudName != null && !cloudinaryCloudName.isBlank()
                && cloudinaryApiKey != null && !cloudinaryApiKey.isBlank()
                && cloudinaryApiSecret != null && !cloudinaryApiSecret.isBlank();
    }

    public ImageUploadResponse uploadImage(MultipartFile file, String folder) {
        if (folder == null || !ALLOWED_FOLDERS.contains(folder.trim().toLowerCase())) {
            throw new AppException(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED,
                    "Folder must be one of: profile, workout, food.",
                    List.of(new FieldErrorItem("folder", "INVALID_FORMAT", "Folder must be profile, workout or food.")));
        }

        if (file == null || file.isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, ErrorCode.UNSUPPORTED_FILE_TYPE,
                    "Only JPG and PNG images are allowed.",
                    List.of(new FieldErrorItem("file", "REQUIRED", "File is required.")));
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AppException(HttpStatus.PAYLOAD_TOO_LARGE, ErrorCode.FILE_TOO_LARGE,
                    "The file is larger than the allowed size.");
        }

        String extension = detectImageExtension(file);
        String cleanFolder = folder.trim().toLowerCase();

        // 1. Try uploading to Cloudinary if configured
        if (cloudinary != null && isCloudinaryConfigured()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> uploadResult = cloudinary.uploader().upload(
                        file.getBytes(),
                        ObjectUtils.asMap(
                                "folder", "dhanushapp/" + cleanFolder,
                                "resource_type", "image"
                        )
                );
                String secureUrl = (String) uploadResult.get("secure_url");
                if (secureUrl != null && !secureUrl.isBlank()) {
                    log.info("Uploaded image to Cloudinary [folder=dhanushapp/{}]: {}", cleanFolder, secureUrl);
                    return new ImageUploadResponse(secureUrl);
                }
            } catch (Exception e) {
                log.warn("Cloudinary upload failed for folder '{}'; falling back to local storage: {}", cleanFolder, e.getMessage());
            }
        }

        // 2. Fallback to local storage
        return uploadToLocalStorage(file, cleanFolder, extension);
    }

    private ImageUploadResponse uploadToLocalStorage(MultipartFile file, String cleanFolder, String extension) {
        try {
            Path targetDir = Paths.get(localUploadDir, cleanFolder);
            Files.createDirectories(targetDir);

            String filename = UUID.randomUUID() + extension;
            Path targetPath = targetDir.resolve(filename);

            try (InputStream is = file.getInputStream()) {
                Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            String url = String.format("%s/uploads/files/%s/%s", publicBaseUrl.replaceAll("/+$", ""), cleanFolder, filename);
            log.info("Saved image to local storage: {}", targetPath.toAbsolutePath());
            return new ImageUploadResponse(url);
        } catch (IOException e) {
            log.error("Failed to store uploaded image locally", e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR, "Failed to save uploaded image.");
        }
    }

    public Resource loadImageAsResource(String folder, String filename) {
        if (folder == null || !ALLOWED_FOLDERS.contains(folder.trim().toLowerCase())) {
            throw new AppException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "File not found.");
        }

        try {
            Path filePath = Paths.get(localUploadDir, folder.trim().toLowerCase()).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new AppException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "File not found.");
            }
        } catch (MalformedURLException e) {
            throw new AppException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "File not found.");
        }
    }

    private String detectImageExtension(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[8];
            int read = is.read(header);
            if (read < 3) {
                throw unsupportedTypeException();
            }

            // JPEG: FF D8 FF
            if ((header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) {
                return ".jpg";
            }

            // PNG: 89 50 4E 47 0D 0A 1A 0A
            if (read >= 8 &&
                    (header[0] & 0xFF) == 0x89 && (header[1] & 0xFF) == 0x50 &&
                    (header[2] & 0xFF) == 0x4E && (header[3] & 0xFF) == 0x47 &&
                    (header[4] & 0xFF) == 0x0D && (header[5] & 0xFF) == 0x0A &&
                    (header[6] & 0xFF) == 0x1A && (header[7] & 0xFF) == 0x0A) {
                return ".png";
            }

            throw unsupportedTypeException();
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw unsupportedTypeException();
        }
    }

    private AppException unsupportedTypeException() {
        return new AppException(
                HttpStatus.BAD_REQUEST,
                ErrorCode.UNSUPPORTED_FILE_TYPE,
                "Only JPG and PNG images are allowed.",
                List.of(new FieldErrorItem("file", "INVALID_FORMAT", "Only JPG and PNG images are allowed."))
        );
    }
}

