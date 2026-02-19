package com.phrontend.springfm.files;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final StoredFileRepository storedFileRepository;
    private final StorageService storageService;

    @Transactional(readOnly = true)
    public StoredFile requireById(UUID id) {
        return storedFileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
    }

    @Transactional(readOnly = true)
    public Resource loadAsResource(StoredFile file) {
        return storageService.loadAsResource(file.getStoragePath());
    }

    @Transactional
    public void delete(UUID id, String userId) throws IOException {
        log.info("Delete request: fileId={}, userId={}", id, userId);

        StoredFile file = requireById(id);

        // Check ownership
        if (!file.getUploadedBy().equals(userId)) {
            log.warn("User {} attempted to delete file {} owned by {}", userId, id, file.getUploadedBy());
            throw new IllegalArgumentException("You do not have permission to delete this file");
        }

        // Delete from filesystem
        storageService.delete(file.getStoragePath());
        log.info("Deleted file from storage: {}", file.getStoragePath());

        // Delete from database
        storedFileRepository.delete(file);
        log.info("Deleted file from database: id={}", id);
    }

    @Transactional
    public StoredFile upload(MultipartFile file, String title, FileCategory category, String metadataText, String uploadedBy) throws IOException {
        log.info("Uploading file: filename={}, size={}, contentType={}, uploadedBy={}",
            file.getOriginalFilename(), file.getSize(), file.getContentType(), uploadedBy);

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            filename = "unnamed-file";
        }

        byte[] fileBytes = file.getBytes();

        // Calculate SHA256 hash
        String sha256 = calculateSha256(fileBytes);
        log.info("Calculated SHA256: {}", sha256);

        // Store file
        String storagePath = storageService.store(fileBytes, filename);

        // Determine category if not provided
        if (category == null) {
            category = detectCategory(file.getContentType(), filename);
        }

        // Use filename as title if not provided
        if (title == null || title.isBlank()) {
            title = filename;
        }

        // Generate metadata if not provided
        if (metadataText == null || metadataText.isBlank()) {
            metadataText = generateMetadata(filename, file.getContentType(), file.getSize(), category);
        }

        // Create database record
        StoredFile storedFile = StoredFile.builder()
                .title(title)
                .filename(filename)
                .category(category)
                .uploadedAt(Instant.now())
                .uploadedBy(uploadedBy)
                .fileSize(file.getSize())
                .metadataText(metadataText)
                .contentType(file.getContentType())
                .storagePath(storagePath)
                .sha256(sha256)
                .build();

        storedFile = storedFileRepository.save(storedFile);
        log.info("File uploaded successfully: id={}, storagePath={}, sha256={}", storedFile.getId(), storagePath, sha256);

        return storedFile;
    }

    private String calculateSha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private String generateMetadata(String filename, String contentType, long fileSize, FileCategory category) {
        StringBuilder metadata = new StringBuilder();

        // Add filename without extension
        String nameWithoutExt = filename.contains(".")
            ? filename.substring(0, filename.lastIndexOf('.'))
            : filename;
        metadata.append(nameWithoutExt.replace("_", " ").replace("-", " "));

        // Add category
        metadata.append(" ").append(category.name().toLowerCase());

        // Add file type description
        if (contentType != null) {
            if (contentType.contains("excel") || contentType.contains("spreadsheet")) {
                metadata.append(" spreadsheet excel");
            } else if (contentType.contains("word") || contentType.contains("document")) {
                metadata.append(" document text");
            } else if (contentType.contains("pdf")) {
                metadata.append(" pdf document");
            } else if (contentType.contains("image")) {
                metadata.append(" image picture");
            } else if (contentType.contains("video")) {
                metadata.append(" video media");
            } else if (contentType.contains("audio")) {
                metadata.append(" audio sound music");
            }
        }

        // Add size category
        if (fileSize < 1024 * 100) {
            metadata.append(" small");
        } else if (fileSize > 1024 * 1024 * 10) {
            metadata.append(" large");
        }

        return metadata.toString();
    }

    private FileCategory detectCategory(String contentType, String filename) {
        if (contentType == null) {
            return FileCategory.OTHER;
        }

        if (contentType.startsWith("image/")) {
            return FileCategory.IMAGE;
        } else if (contentType.startsWith("video/")) {
            return FileCategory.VIDEO;
        } else if (contentType.startsWith("audio/")) {
            return FileCategory.AUDIO;
        } else if (contentType.contains("pdf") || contentType.contains("document") ||
                   contentType.contains("word") || contentType.contains("excel") ||
                   contentType.contains("spreadsheet") || contentType.contains("presentation")) {
            return FileCategory.DOCUMENT;
        } else if (contentType.contains("zip") || contentType.contains("compress") ||
                   contentType.contains("archive") || contentType.contains("rar") ||
                   contentType.contains("tar")) {
            return FileCategory.ARCHIVE;
        } else if (filename.matches(".*\\.(java|js|ts|py|cpp|c|h|cs|go|rs|php|rb|kt|swift)$")) {
            return FileCategory.CODE;
        }

        return FileCategory.OTHER;
    }
}
