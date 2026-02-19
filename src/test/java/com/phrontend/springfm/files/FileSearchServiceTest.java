package com.phrontend.springfm.files;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.mockito.ArgumentMatchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileSearchServiceTest {

    @Mock
    private StoredFileRepository storedFileRepository;

    @InjectMocks
    private FileSearchService fileSearchService;

    private StoredFile testFile1;
    private StoredFile testFile2;
    private StoredFile testFile3;

    @BeforeEach
    void setUp() {
        testFile1 = StoredFile.builder()
                .id(UUID.randomUUID())
                .title("Test Document")
                .filename("test-doc.pdf")
                .category(FileCategory.DOCUMENT)
                .uploadedAt(Instant.now())
                .uploadedBy("user@example.com")
                .fileSize(1024)
                .storagePath("/storage/test-doc.pdf")
                .build();

        testFile2 = StoredFile.builder()
                .id(UUID.randomUUID())
                .title("Test Image")
                .filename("test-image.jpg")
                .category(FileCategory.IMAGE)
                .uploadedAt(Instant.now())
                .uploadedBy("user@example.com")
                .fileSize(2048)
                .storagePath("/storage/test-image.jpg")
                .build();

        testFile3 = StoredFile.builder()
                .id(UUID.randomUUID())
                .title("Sample Video")
                .filename("sample-video.mp4")
                .category(FileCategory.VIDEO)
                .uploadedAt(Instant.now())
                .uploadedBy("user@example.com")
                .fileSize(4096)
                .storagePath("/storage/sample-video.mp4")
                .build();
    }

    @Test
    void search_WithQueryAndCategories_ReturnsFilteredResults() {
        // Arrange
        String query = "test";
        List<FileCategory> categories = Arrays.asList(FileCategory.DOCUMENT, FileCategory.IMAGE);
        PageRequest pageRequest = PageRequest.of(0, 20);

        Page<StoredFile> expectedPage = new PageImpl<>(Arrays.asList(testFile1, testFile2));
        when(storedFileRepository.findAll(ArgumentMatchers.<Specification<StoredFile>>any(), eq(pageRequest)))
                .thenReturn(expectedPage);

        // Act
        Page<StoredFile> result = fileSearchService.search(query, categories, pageRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).containsExactly(testFile1, testFile2);

        verify(storedFileRepository).findAll(ArgumentMatchers.<Specification<StoredFile>>any(), eq(pageRequest));
    }

    @Test
    void search_WithNullQuery_ReturnsAllResults() {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 20);

        Page<StoredFile> expectedPage = new PageImpl<>(Arrays.asList(testFile1, testFile2, testFile3));
        when(storedFileRepository.findAll(ArgumentMatchers.<Specification<StoredFile>>any(), eq(pageRequest)))
                .thenReturn(expectedPage);

        // Act
        Page<StoredFile> result = fileSearchService.search(null, null, pageRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);

        verify(storedFileRepository).findAll(ArgumentMatchers.<Specification<StoredFile>>any(), eq(pageRequest));
    }

    @Test
    void search_WithEmptyCategories_ReturnsAllMatchingQuery() {
        // Arrange
        String query = "test";
        PageRequest pageRequest = PageRequest.of(0, 20);

        Page<StoredFile> expectedPage = new PageImpl<>(Arrays.asList(testFile1, testFile2));
        when(storedFileRepository.findAll(ArgumentMatchers.<Specification<StoredFile>>any(), eq(pageRequest)))
                .thenReturn(expectedPage);

        // Act
        Page<StoredFile> result = fileSearchService.search(query, Collections.emptyList(), pageRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);

        verify(storedFileRepository).findAll(ArgumentMatchers.<Specification<StoredFile>>any(), eq(pageRequest));
    }

    @Test
    void suggest_WithValidQuery_ReturnsSuggestions() {
        // Arrange
        String query = "test";
        
        Page<StoredFile> expectedPage = new PageImpl<>(Arrays.asList(testFile1, testFile2));
        when(storedFileRepository.findByTitleContainingIgnoreCaseOrFilenameContainingIgnoreCase(
                eq(query),
                eq(query),
                any(PageRequest.class)
        )).thenReturn(expectedPage);

        // Act
        List<String> suggestions = fileSearchService.suggest(query, null);

        // Assert
        assertThat(suggestions).isNotEmpty();
        assertThat(suggestions).contains("Test Document", "Test Image");
        assertThat(suggestions.size()).isLessThanOrEqualTo(10);
    }

    @Test
    void suggest_WithNullQuery_ReturnsEmptyList() {
        // Act
        List<String> suggestions = fileSearchService.suggest(null, null);

        // Assert
        assertThat(suggestions).isEmpty();
    }

    @Test
    void suggest_WithBlankQuery_ReturnsEmptyList() {
        // Act
        List<String> suggestions = fileSearchService.suggest("   ", null);

        // Assert
        assertThat(suggestions).isEmpty();
    }

    @Test
    void suggest_WithCategoryFilter_FiltersSuggestions() {
        // Arrange
        String query = "test";
        List<FileCategory> categories = Collections.singletonList(FileCategory.DOCUMENT);
        
        Page<StoredFile> expectedPage = new PageImpl<>(Arrays.asList(testFile1, testFile2));
        when(storedFileRepository.findByTitleContainingIgnoreCaseOrFilenameContainingIgnoreCase(
                eq(query),
                eq(query),
                any(PageRequest.class)
        )).thenReturn(expectedPage);

        // Act
        List<String> suggestions = fileSearchService.suggest(query, categories);

        // Assert
        assertThat(suggestions).isNotEmpty();
        assertThat(suggestions).contains("Test Document");
        assertThat(suggestions).doesNotContain("Test Image");
    }

    @Test
    void suggest_LimitsTo10Suggestions() {
        // Arrange
        String query = "file";
        
        // Create 15 files
        List<StoredFile> manyFiles = Arrays.asList(
                createFile("File 1", "file1.txt"),
                createFile("File 2", "file2.txt"),
                createFile("File 3", "file3.txt"),
                createFile("File 4", "file4.txt"),
                createFile("File 5", "file5.txt"),
                createFile("File 6", "file6.txt"),
                createFile("File 7", "file7.txt"),
                createFile("File 8", "file8.txt"),
                createFile("File 9", "file9.txt"),
                createFile("File 10", "file10.txt"),
                createFile("File 11", "file11.txt"),
                createFile("File 12", "file12.txt"),
                createFile("File 13", "file13.txt"),
                createFile("File 14", "file14.txt"),
                createFile("File 15", "file15.txt")
        );
        
        Page<StoredFile> expectedPage = new PageImpl<>(manyFiles);
        when(storedFileRepository.findByTitleContainingIgnoreCaseOrFilenameContainingIgnoreCase(
                eq(query),
                eq(query),
                any(PageRequest.class)
        )).thenReturn(expectedPage);

        // Act
        List<String> suggestions = fileSearchService.suggest(query, null);

        // Assert
        assertThat(suggestions).hasSizeLessThanOrEqualTo(10);
    }

    @Test
    void suggest_RemovesDuplicates() {
        // Arrange
        String query = "duplicate";
        
        StoredFile duplicateFile = StoredFile.builder()
                .id(UUID.randomUUID())
                .title("Duplicate File")
                .filename("duplicate-file.txt")
                .category(FileCategory.DOCUMENT)
                .uploadedAt(Instant.now())
                .uploadedBy("user@example.com")
                .fileSize(512)
                .storagePath("/storage/duplicate-file.txt")
                .build();
        
        Page<StoredFile> expectedPage = new PageImpl<>(Arrays.asList(duplicateFile, duplicateFile));
        when(storedFileRepository.findByTitleContainingIgnoreCaseOrFilenameContainingIgnoreCase(
                eq(query),
                eq(query),
                any(PageRequest.class)
        )).thenReturn(expectedPage);

        // Act
        List<String> suggestions = fileSearchService.suggest(query, null);

        // Assert
        // Should only have 2 unique suggestions (title and filename), not 4
        assertThat(suggestions).hasSizeLessThanOrEqualTo(2);
    }

    private StoredFile createFile(String title, String filename) {
        return StoredFile.builder()
                .id(UUID.randomUUID())
                .title(title)
                .filename(filename)
                .category(FileCategory.DOCUMENT)
                .uploadedAt(Instant.now())
                .uploadedBy("user@example.com")
                .fileSize(1024)
                .storagePath("/storage/" + filename)
                .build();
    }
}
