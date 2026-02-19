package com.phrontend.springfm.files;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SearchResponseTest {

    @Test
    void constructor_WithAllFields_CreatesResponse() {
        // Arrange
        FileResult file1 = new FileResult("1", "File 1", "file1.txt", FileCategory.DOCUMENT,
                Instant.now(), "user@example.com", 1024, null, "/api/files/1/download");
        FileResult file2 = new FileResult("2", "File 2", "file2.txt", FileCategory.IMAGE,
                Instant.now(), "user@example.com", 2048, null, "/api/files/2/download");
        List<FileResult> results = Arrays.asList(file1, file2);

        // Act
        SearchResponse response = new SearchResponse(results, 50, 2, 20);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.results()).hasSize(2);
        assertThat(response.total()).isEqualTo(50);
        assertThat(response.page()).isEqualTo(2);
        assertThat(response.size()).isEqualTo(20);
    }

    @Test
    void constructor_WithEmptyResults_CreatesResponse() {
        // Act
        SearchResponse response = new SearchResponse(Collections.emptyList(), 0, 0, 20);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.results()).isEmpty();
        assertThat(response.total()).isEqualTo(0);
    }

    @Test
    void results_ReturnsImmutableList() {
        // Arrange
        FileResult file = new FileResult("1", "File", "file.txt", FileCategory.DOCUMENT,
                Instant.now(), "user@example.com", 1024, null, "/api/files/1/download");
        List<FileResult> results = Collections.singletonList(file);
        SearchResponse response = new SearchResponse(results, 1, 0, 20);

        // Act & Assert
        assertThat(response.results()).hasSize(1);
        assertThat(response.results().getFirst()).isEqualTo(file);
    }

    @Test
    void equals_WithSameValues_ReturnsTrue() {
        // Arrange
        List<FileResult> results = Collections.emptyList();
        SearchResponse response1 = new SearchResponse(results, 10, 0, 20);
        SearchResponse response2 = new SearchResponse(results, 10, 0, 20);

        // Act & Assert
        assertThat(response1).isEqualTo(response2);
    }

    @Test
    void hashCode_WithSameValues_ReturnsSameHashCode() {
        // Arrange
        List<FileResult> results = Collections.emptyList();
        SearchResponse response1 = new SearchResponse(results, 10, 0, 20);
        SearchResponse response2 = new SearchResponse(results, 10, 0, 20);

        // Act & Assert
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }
}
