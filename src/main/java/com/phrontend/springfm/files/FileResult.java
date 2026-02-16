package com.phrontend.springfm.files;

import java.time.Instant;

public record FileResult(
        String id,
        String title,
        String filename,
        FileCategory category,
        Instant uploadedAt,
        String uploadedBy,
        long fileSize,
        String metadataText,
        String downloadUrl
) {
    public static FileResult fromEntity(StoredFile file) {
        return new FileResult(
                file.getId().toString(),
                file.getTitle(),
                file.getFilename(),
                file.getCategory(),
                file.getUploadedAt(),
                file.getUploadedBy(),
                file.getFileSize(),
                file.getMetadataText(),
                "/api/files/" + file.getId() + "/download"
        );
    }
}
