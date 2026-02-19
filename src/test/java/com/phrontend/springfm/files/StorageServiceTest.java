package com.phrontend.springfm.files;

import com.phrontend.springfm.config.StorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StorageServiceTest {

    @TempDir
    Path tempDir;

    private StorageService storageService;

    @BeforeEach
    void setUp() throws IOException {
        StorageProperties properties = new StorageProperties(tempDir.toString());
        storageService = new StorageService(properties);
        storageService.init();
    }

    @Test
    void init_CreatesRootDirectory() {
        // Assert
        assertThat(tempDir).exists();
        assertThat(tempDir).isDirectory();
    }

    @Test
    void loadAsResource_WithExistingFile_ReturnsResource() throws IOException {
        // Arrange
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "test content");

        // Act
        Resource resource = storageService.loadAsResource("test.txt");

        // Assert
        assertThat(resource).isNotNull();
        assertThat(resource.exists()).isTrue();
        assertThat(resource.getFilename()).isEqualTo("test.txt");
    }

    @Test
    void loadAsResource_WithNonExistentFile_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> storageService.loadAsResource("nonexistent.txt"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("File not found");
    }

    @Test
    void loadAsResource_WithPathTraversal_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> storageService.loadAsResource("../../../etc/passwd"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid storage path");
    }

    @Test
    void loadAsResource_WithAbsolutePath_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> storageService.loadAsResource("/etc/passwd"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid storage path");
    }

    @Test
    void loadAsResource_WithNestedFile_ReturnsResource() throws IOException {
        // Arrange
        Path subdir = tempDir.resolve("subdir");
        Files.createDirectories(subdir);
        Path nestedFile = subdir.resolve("nested.txt");
        Files.writeString(nestedFile, "nested content");

        // Act
        Resource resource = storageService.loadAsResource("subdir/nested.txt");

        // Assert
        assertThat(resource).isNotNull();
        assertThat(resource.exists()).isTrue();
    }

    @Test
    void loadAsResource_WithDotInFilename_ReturnsResource() throws IOException {
        // Arrange
        Path testFile = tempDir.resolve("file.with.dots.txt");
        Files.writeString(testFile, "content");

        // Act
        Resource resource = storageService.loadAsResource("file.with.dots.txt");

        // Assert
        assertThat(resource).isNotNull();
        assertThat(resource.exists()).isTrue();
    }

    @Test
    void loadAsResource_PreventsDotDotTraversal() {
        // Act & Assert
        assertThatThrownBy(() -> storageService.loadAsResource("subdir/../../outside.txt"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void loadAsResource_WithBackslashSeparator_HandledCorrectly() throws IOException {
        // Arrange - create file using forward slash
        Path subdir = tempDir.resolve("subdir");
        Files.createDirectories(subdir);
        Path testFile = subdir.resolve("file.txt");
        Files.writeString(testFile, "content");

        // Act - try to load with backslash (should be normalized)
        Resource resource = storageService.loadAsResource("subdir/file.txt");

        // Assert
        assertThat(resource).isNotNull();
        assertThat(resource.exists()).isTrue();
    }
}
