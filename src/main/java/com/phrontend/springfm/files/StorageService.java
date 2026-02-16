package com.phrontend.springfm.files;

import com.phrontend.springfm.config.StorageProperties;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final StorageProperties storageProperties;
    private Path rootPath;

    @PostConstruct
    public void init() throws IOException {
        rootPath = Paths.get(storageProperties.root()).toAbsolutePath().normalize();
        Files.createDirectories(rootPath);
    }

    public Resource loadAsResource(String storagePath) {
        Path resolved = resolve(storagePath);
        return new FileSystemResource(resolved);
    }

    private Path resolve(String storagePath) {
        Path resolved = rootPath.resolve(storagePath).normalize();
        if (!resolved.startsWith(rootPath)) {
            throw new IllegalArgumentException("Invalid storage path");
        }
        if (!Files.exists(resolved)) {
            throw new IllegalArgumentException("File not found");
        }
        return resolved;
    }
}
