package com.phrontend.springfm.files;

import com.phrontend.springfm.config.StorageProperties;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
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

    public String store(byte[] content, String filename) throws IOException {
        String storagePath = generateStoragePath(filename);
        Path targetPath = rootPath.resolve(storagePath);
        Files.createDirectories(targetPath.getParent());
        Files.write(targetPath, content);
        return storagePath;
    }

    public void delete(String storagePath) throws IOException {
        Path resolved = rootPath.resolve(storagePath).normalize();
        if (!resolved.startsWith(rootPath)) {
            throw new IllegalArgumentException("Invalid storage path");
        }
        if (Files.exists(resolved)) {
            Files.delete(resolved);
        }
    }

    private String generateStoragePath(String filename) {
        String uuid = UUID.randomUUID().toString();
        return uuid.substring(0, 2) + "/" + uuid.substring(2, 4) + "/" + uuid + "-" + filename;
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
