package com.phrontend.springfm.files;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FileResultTest {

    @Test
    void fromEntity_MapsAllFieldsCorrectly() {
        // Arrange
        UUID fileId = UUID.randomUUID();
        Instant uploadedAt = Instant.now();

        StoredFile file = StoredFile.builder()
                .id(fileId)
                .title("Test Document")
                .filename("test-document.pdf")
                .category(FileCategory.DOCUMENT)
                .uploadedAt(uploadedAt)
                .uploadedBy("test@example.com")
                .fileSize(1024000)
                .metadataText("Test metadata content")
                .contentType("application/pdf")
                .storagePath("/storage/test-document.pdf")
                .sha256("abc123def456")
                .build();

        // Act
        FileResult result = FileResult.fromEntity(file, "Test User");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(fileId.toString());
        assertThat(result.title()).isEqualTo("Test Document");
        assertThat(result.filename()).isEqualTo("test-document.pdf");
        assertThat(result.category()).isEqualTo(FileCategory.DOCUMENT);
        assertThat(result.uploadedAt()).isEqualTo(uploadedAt);
        assertThat(result.uploadedBy()).isEqualTo("Test User");
        assertThat(result.uploadedByUserId()).isEqualTo("test@example.com");
        assertThat(result.fileSize()).isEqualTo(1024000);
        assertThat(result.metadataText()).isEqualTo("Test metadata content");
        assertThat(result.downloadUrl()).isEqualTo("/api/files/" + fileId + "/download");
    }

    @Test
    void fromEntity_WithImageCategory_GeneratesCorrectDownloadUrl() {
        // Arrange
        UUID fileId = UUID.randomUUID();

        StoredFile file = StoredFile.builder()
                .id(fileId)
                .title("Test Image")
                .filename("photo.jpg")
                .category(FileCategory.IMAGE)
                .uploadedAt(Instant.now())
                .uploadedBy("user@example.com")
                .fileSize(2048000)
                .storagePath("/storage/photo.jpg")
                .build();

        // Act
        FileResult result = FileResult.fromEntity(file, "Test User");

        // Assert
        assertThat(result.category()).isEqualTo(FileCategory.IMAGE);
        assertThat(result.downloadUrl()).isEqualTo("/api/files/" + fileId + "/download");
    }

    @Test
    void fromEntity_WithNullMetadata_HandlesGracefully() {
        // Arrange
        UUID fileId = UUID.randomUUID();

        StoredFile file = StoredFile.builder()
                .id(fileId)
                .title("File Without Metadata")
                .filename("file.txt")
                .category(FileCategory.OTHER)
                .uploadedAt(Instant.now())
                .uploadedBy("user@example.com")
                .fileSize(512)
                .storagePath("/storage/file.txt")
                .metadataText(null)
                .build();

        // Act
        FileResult result = FileResult.fromEntity(file, "Test User");

        // Assert
        assertThat(result.metadataText()).isNull();
    }

    @Test
    void fromEntity_WithAllFileCategories_MapsCorrectly() {
        // Test each file category
        for (FileCategory category : FileCategory.values()) {
            UUID fileId = UUID.randomUUID();

            StoredFile file = StoredFile.builder()
                    .id(fileId)
                    .title("Test " + category.name())
                    .filename("test." + category.name().toLowerCase())
                    .category(category)
                    .uploadedAt(Instant.now())
                    .uploadedBy("user@example.com")
                    .fileSize(1000)
                    .storagePath("/storage/test")
                    .build();

            FileResult result = FileResult.fromEntity(file, "Test User");

            assertThat(result.category()).isEqualTo(category);
        }
    }
}
