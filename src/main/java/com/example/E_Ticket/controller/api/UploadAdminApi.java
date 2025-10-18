package com.example.E_Ticket.controller.api;


import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/v1/upload")
public class UploadAdminApi {
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String,String> upload(@RequestPart("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new IllegalArgumentException("File empty");
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/"))
            throw new IllegalArgumentException("Only image allowed");

        Path dir = Paths.get("uploads"); Files.createDirectories(dir);

        // tên file an toàn + duy nhất
        String ext = Optional.ofNullable(file.getOriginalFilename())
                .filter(fn -> fn.contains("."))
                .map(fn -> fn.substring(fn.lastIndexOf(".")))
                .orElse(".bin");
        String name = System.currentTimeMillis() + "-" + UUID.randomUUID() + ext;

        Path p = dir.resolve(name);
        Files.copy(file.getInputStream(), p, StandardCopyOption.REPLACE_EXISTING);

        return Map.of("url", "/uploads/" + name);
    }
}
