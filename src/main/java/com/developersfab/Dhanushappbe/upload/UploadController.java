package com.developersfab.Dhanushappbe.upload;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final ImageUploadService imageUploadService;

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folder") String folder) {
        ImageUploadResponse response = imageUploadService.uploadImage(file, folder);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/files/{folder}/{filename:.+}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable String folder,
            @PathVariable String filename) {
        Resource file = imageUploadService.loadImageAsResource(folder, filename);

        String contentType = "application/octet-stream";
        try {
            String probe = Files.probeContentType(Paths.get(file.getURI()));
            if (probe != null) {
                contentType = probe;
            }
        } catch (IOException ignored) {
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }
}

