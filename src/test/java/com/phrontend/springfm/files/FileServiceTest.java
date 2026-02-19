package com.phrontend.springfm.files;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private StoredFileRepository storedFileRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private Resource mockResource;

    @InjectMocks
    private FileService fileService;

    private StoredFile testFile;
    private UUID testFileId;

    @BeforeEach
    void setUp() {
        testFileId = UUID.randomUUID();
        testFile = StoredFile.builder()
                .id(testFileId)
                .title("Test Document")
                .filename("test-doc.pdf")
                .category(FileCategory.DOCUMENT)
                .uploadedAt(Instant.now())
                .uploadedBy("user@example.com")
                .fileSize(1024)
                .storagePath("/storage/test-doc.pdf")
                .contentType("application/pdf")
                .build();
    }

    @Test
    void requireById_WithExistingFile_ReturnsFile() {
        // Arrange
        when(storedFileRepository.findById(testFileId))
                .thenReturn(Optional.of(testFile));

        // Act
        StoredFile result = fileService.requireById(testFileId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testFileId);
        assertThat(result.getTitle()).isEqualTo("Test Document");
        assertThat(result.getFilename()).isEqualTo("test-doc.pdf");

        verify(storedFileRepository).findById(testFileId);
    }

    @Test
    void requireById_WithNonExistentFile_ThrowsException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(storedFileRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> fileService.requireById(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("File not found");

        verify(storedFileRepository).findById(nonExistentId);
    }

    @Test
    void loadAsResource_WithValidFile_ReturnsResource() {
        // Arrange
        when(storageService.loadAsResource("/storage/test-doc.pdf"))
                .thenReturn(mockResource);

        // Act
        Resource result = fileService.loadAsResource(testFile);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(mockResource);

        verify(storageService).loadAsResource("/storage/test-doc.pdf");
    }

    @Test
    void loadAsResource_DelegatesToStorageService() {
        // Arrange
        String storagePath = "/different/path/file.txt";
        testFile.setStoragePath(storagePath);
        
        when(storageService.loadAsResource(storagePath))
                .thenReturn(mockResource);

        // Act
        fileService.loadAsResource(testFile);

        // Assert
        verify(storageService).loadAsResource(storagePath);
    }

    @Test
    void requireById_CalledMultipleTimes_QueriesRepositoryEachTime() {
        // Arrange
        when(storedFileRepository.findById(testFileId))
                .thenReturn(Optional.of(testFile));

        // Act
        fileService.requireById(testFileId);
        fileService.requireById(testFileId);
        fileService.requireById(testFileId);

        // Assert
        verify(storedFileRepository, times(3)).findById(testFileId);
    }
}
