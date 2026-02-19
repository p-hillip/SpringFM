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
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    @Mock
    private FileSearchService fileSearchService;

    @InjectMocks
    private SearchController searchController;

    private StoredFile testFile1;
    private StoredFile testFile2;

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
    }

    @Test
    void search_WithDefaultParameters_ReturnsSearchResponse() {
        // Arrange
        Page<StoredFile> page = new PageImpl<>(Arrays.asList(testFile1, testFile2), 
                PageRequest.of(0, 20), 2);
        
        when(fileSearchService.search(isNull(), isNull(), any(PageRequest.class)))
                .thenReturn(page);

        // Act
        SearchResponse response = searchController.search(null, null, "uploadedAt", "desc", 0, 20);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.results()).hasSize(2);
        assertThat(response.total()).isEqualTo(2);
        assertThat(response.page()).isEqualTo(1); // 1-based pagination for frontend
        assertThat(response.size()).isEqualTo(20);
        
        verify(fileSearchService).search(isNull(), isNull(), any(PageRequest.class));
    }

    @Test
    void search_WithQuery_PassesQueryToService() {
        // Arrange
        String query = "test";
        Page<StoredFile> page = new PageImpl<>(Collections.singletonList(testFile1));
        
        when(fileSearchService.search(eq(query), isNull(), any(PageRequest.class)))
                .thenReturn(page);

        // Act
        SearchResponse response = searchController.search(query, null, "uploadedAt", "desc", 0, 20);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.results()).hasSize(1);
        
        verify(fileSearchService).search(eq(query), isNull(), any(PageRequest.class));
    }

    @Test
    void search_WithCategories_PassesCategoriesToService() {
        // Arrange
        List<FileCategory> categories = Arrays.asList(FileCategory.DOCUMENT, FileCategory.IMAGE);
        Page<StoredFile> page = new PageImpl<>(Arrays.asList(testFile1, testFile2));
        
        when(fileSearchService.search(isNull(), eq(categories), any(PageRequest.class)))
                .thenReturn(page);

        // Act
        SearchResponse response = searchController.search(null, categories, "uploadedAt", "desc", 0, 20);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.results()).hasSize(2);
        
        verify(fileSearchService).search(isNull(), eq(categories), any(PageRequest.class));
    }

    @Test
    void search_WithAscendingSortDirection_UsesAscending() {
        // Arrange
        Page<StoredFile> page = new PageImpl<>(Collections.emptyList());
        
        when(fileSearchService.search(isNull(), isNull(), any(PageRequest.class)))
                .thenReturn(page);

        // Act
        searchController.search(null, null, "title", "asc", 0, 20);

        // Assert
        verify(fileSearchService).search(isNull(), isNull(), 
                argThat(pr -> Objects.requireNonNull(pr.getSort().getOrderFor("title")).getDirection() == Sort.Direction.ASC));
    }

    @Test
    void search_WithDescendingSortDirection_UsesDescending() {
        // Arrange
        Page<StoredFile> page = new PageImpl<>(Collections.emptyList());
        
        when(fileSearchService.search(isNull(), isNull(), any(PageRequest.class)))
                .thenReturn(page);

        // Act
        searchController.search(null, null, "fileSize", "desc", 0, 20);

        // Assert
        verify(fileSearchService).search(isNull(), isNull(), 
                argThat(pr -> Objects.requireNonNull(pr.getSort().getOrderFor("fileSize")).getDirection() == Sort.Direction.DESC));
    }

    @Test
    void search_WithInvalidSortField_FallsBackToUploadedAt() {
        // Arrange
        Page<StoredFile> page = new PageImpl<>(Collections.emptyList());
        
        when(fileSearchService.search(isNull(), isNull(), any(PageRequest.class)))
                .thenReturn(page);

        // Act
        searchController.search(null, null, "invalidField", "desc", 0, 20);

        // Assert
        verify(fileSearchService).search(isNull(), isNull(), 
                argThat(pr -> pr.getSort().getOrderFor("uploadedAt") != null));
    }

    @Test
    void search_WithValidSortFields_UsesCorrectField() {
        // Arrange
        Page<StoredFile> page = new PageImpl<>(Collections.emptyList());
        
        when(fileSearchService.search(isNull(), isNull(), any(PageRequest.class)))
                .thenReturn(page);

        // Test all valid sort fields
        String[] validFields = {"title", "filename", "category", "uploadedAt", "uploadedBy", "fileSize"};
        
        for (String field : validFields) {
            searchController.search(null, null, field, "desc", 0, 20);
        }

        // Assert
        verify(fileSearchService, times(validFields.length))
                .search(isNull(), isNull(), any(PageRequest.class));
    }

    @Test
    void search_WithCustomPageAndSize_UsesProvidedValues() {
        // Arrange
        Page<StoredFile> page = new PageImpl<>(Collections.emptyList(),
                PageRequest.of(1, 10), 0); // Backend receives page-1 (2-1=1)

        when(fileSearchService.search(isNull(), isNull(), any(PageRequest.class)))
                .thenReturn(page);

        // Act
        SearchResponse response = searchController.search(null, null, "uploadedAt", "desc", 2, 10);

        // Assert
        assertThat(response.page()).isEqualTo(2); // Returns 1-based page (1+1=2)
        assertThat(response.size()).isEqualTo(10);

        verify(fileSearchService).search(isNull(), isNull(),
                argThat(pr -> pr.getPageNumber() == 1 && pr.getPageSize() == 10)); // Verify 0-based backend call
    }

    @Test
    void suggest_WithQuery_ReturnsSuggestions() {
        // Arrange
        String query = "test";
        List<String> suggestions = Arrays.asList("Test Document", "Test Image");
        
        when(fileSearchService.suggest(eq(query), isNull()))
                .thenReturn(suggestions);

        // Act
        SearchSuggestionResponse response = searchController.suggest(query, null);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.suggestions()).hasSize(2);
        assertThat(response.suggestions()).contains("Test Document", "Test Image");
        
        verify(fileSearchService).suggest(eq(query), isNull());
    }

    @Test
    void suggest_WithCategories_PassesCategoriesToService() {
        // Arrange
        String query = "doc";
        List<FileCategory> categories = Collections.singletonList(FileCategory.DOCUMENT);
        List<String> suggestions = Collections.singletonList("Document");
        
        when(fileSearchService.suggest(eq(query), eq(categories)))
                .thenReturn(suggestions);

        // Act
        SearchSuggestionResponse response = searchController.suggest(query, categories);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.suggestions()).hasSize(1);
        
        verify(fileSearchService).suggest(eq(query), eq(categories));
    }

    @Test
    void suggest_WithNullQuery_ReturnsEmptySuggestions() {
        // Arrange
        when(fileSearchService.suggest(isNull(), isNull()))
                .thenReturn(Collections.emptyList());

        // Act
        SearchSuggestionResponse response = searchController.suggest(null, null);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.suggestions()).isEmpty();
        
        verify(fileSearchService).suggest(isNull(), isNull());
    }
}
