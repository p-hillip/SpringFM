package com.phrontend.springfm.files;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock
    private FileService fileService;

    @Mock
    private Resource mockResource;

    @InjectMocks
    private FileController fileController;

    private UUID testFileId;
    private StoredFile testFile;

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
    void download_WithValidId_ReturnsFileResource() {
        // Arrange
        when(fileService.requireById(testFileId)).thenReturn(testFile);
        when(fileService.loadAsResource(testFile)).thenReturn(mockResource);

        // Act
        ResponseEntity<Resource> response = fileController.download(testFileId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(mockResource);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
        assertThat(response.getHeaders().getContentLength()).isEqualTo(1024);
        assertThat(response.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION))
                .contains("attachment; filename=\"test-doc.pdf\"");

        verify(fileService).requireById(testFileId);
        verify(fileService).loadAsResource(testFile);
    }

    @Test
    void download_WithImageFile_ReturnsCorrectContentType() {
        // Arrange
        testFile.setContentType("image/jpeg");
        testFile.setFilename("photo.jpg");
        
        when(fileService.requireById(testFileId)).thenReturn(testFile);
        when(fileService.loadAsResource(testFile)).thenReturn(mockResource);

        // Act
        ResponseEntity<Resource> response = fileController.download(testFileId);

        // Assert
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.IMAGE_JPEG);
        assertThat(response.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION))
                .contains("attachment; filename=\"photo.jpg\"");
    }

    @Test
    void download_WithNullContentType_UsesOctetStream() {
        // Arrange
        testFile.setContentType(null);
        
        when(fileService.requireById(testFileId)).thenReturn(testFile);
        when(fileService.loadAsResource(testFile)).thenReturn(mockResource);

        // Act
        ResponseEntity<Resource> response = fileController.download(testFileId);

        // Assert
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM);
    }

    @Test
    void download_WithBlankContentType_UsesOctetStream() {
        // Arrange
        testFile.setContentType("   ");
        
        when(fileService.requireById(testFileId)).thenReturn(testFile);
        when(fileService.loadAsResource(testFile)).thenReturn(mockResource);

        // Act
        ResponseEntity<Resource> response = fileController.download(testFileId);

        // Assert
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM);
    }

    @Test
    void download_WithInvalidId_ThrowsException() {
        // Arrange
        UUID invalidId = UUID.randomUUID();
        when(fileService.requireById(invalidId))
                .thenThrow(new IllegalArgumentException("File not found"));

        // Act & Assert
        assertThatThrownBy(() -> fileController.download(invalidId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("File not found");

        verify(fileService).requireById(invalidId);
    }

    @Test
    void download_SetsCorrectContentLength() {
        // Arrange
        testFile.setFileSize(2048000);
        
        when(fileService.requireById(testFileId)).thenReturn(testFile);
        when(fileService.loadAsResource(testFile)).thenReturn(mockResource);

        // Act
        ResponseEntity<Resource> response = fileController.download(testFileId);

        // Assert
        assertThat(response.getHeaders().getContentLength()).isEqualTo(2048000);
    }

    @Test
    void download_WithSpecialCharactersInFilename_HandlesCorrectly() {
        // Arrange
        testFile.setFilename("file with spaces & special.pdf");
        
        when(fileService.requireById(testFileId)).thenReturn(testFile);
        when(fileService.loadAsResource(testFile)).thenReturn(mockResource);

        // Act
        ResponseEntity<Resource> response = fileController.download(testFileId);

        // Assert
        assertThat(response.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION)).isNotNull();
        assertThat(Objects.requireNonNull(response.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION)).getFirst())
                .contains("attachment");
    }

    @Test
    void download_WithVideoFile_ReturnsCorrectContentType() {
        // Arrange
        testFile.setContentType("video/mp4");
        testFile.setFilename("video.mp4");
        testFile.setCategory(FileCategory.VIDEO);
        
        when(fileService.requireById(testFileId)).thenReturn(testFile);
        when(fileService.loadAsResource(testFile)).thenReturn(mockResource);

        // Act
        ResponseEntity<Resource> response = fileController.download(testFileId);

        // Assert
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.parseMediaType("video/mp4"));
    }

    @Test
    void download_WithAudioFile_ReturnsCorrectContentType() {
        // Arrange
        testFile.setContentType("audio/mpeg");
        testFile.setFilename("song.mp3");
        testFile.setCategory(FileCategory.AUDIO);
        
        when(fileService.requireById(testFileId)).thenReturn(testFile);
        when(fileService.loadAsResource(testFile)).thenReturn(mockResource);

        // Act
        ResponseEntity<Resource> response = fileController.download(testFileId);

        // Assert
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.parseMediaType("audio/mpeg"));
    }
}
