package com.evida.backend.service;

import com.evida.backend.config.AppProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalStorageService implements StorageService {
    private final Path rootPath;

    public LocalStorageService(AppProperties appProperties) throws IOException {
        this.rootPath = Path.of(appProperties.getStorage().getUploadDir()).toAbsolutePath().normalize();
        Files.createDirectories(this.rootPath);
    }

    @Override
    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드 파일이 비어 있습니다.");
        }
        String safeName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        try {
            Files.copy(file.getInputStream(), rootPath.resolve(safeName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("파일 저장 실패", e);
        }
        return safeName;
    }
}