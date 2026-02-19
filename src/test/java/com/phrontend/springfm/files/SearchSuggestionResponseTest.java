package com.phrontend.springfm.files;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SearchSuggestionResponseTest {

    @Test
    void constructor_WithSuggestions_CreatesResponse() {
        // Arrange
        List<String> suggestions = Arrays.asList("Document 1", "Document 2", "Image 1");

        // Act
        SearchSuggestionResponse response = new SearchSuggestionResponse(suggestions);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.suggestions()).hasSize(3);
        assertThat(response.suggestions()).contains("Document 1", "Document 2", "Image 1");
    }

    @Test
    void constructor_WithEmptySuggestions_CreatesResponse() {
        // Act
        SearchSuggestionResponse response = new SearchSuggestionResponse(Collections.emptyList());

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.suggestions()).isEmpty();
    }

    @Test
    void suggestions_ReturnsList() {
        // Arrange
        List<String> suggestions = Collections.singletonList("Test Suggestion");
        SearchSuggestionResponse response = new SearchSuggestionResponse(suggestions);

        // Act & Assert
        assertThat(response.suggestions()).hasSize(1);
        assertThat(response.suggestions().getFirst()).isEqualTo("Test Suggestion");
    }

    @Test
    void equals_WithSameValues_ReturnsTrue() {
        // Arrange
        List<String> suggestions = Arrays.asList("A", "B", "C");
        SearchSuggestionResponse response1 = new SearchSuggestionResponse(suggestions);
        SearchSuggestionResponse response2 = new SearchSuggestionResponse(suggestions);

        // Act & Assert
        assertThat(response1).isEqualTo(response2);
    }

    @Test
    void hashCode_WithSameValues_ReturnsSameHashCode() {
        // Arrange
        List<String> suggestions = Arrays.asList("A", "B", "C");
        SearchSuggestionResponse response1 = new SearchSuggestionResponse(suggestions);
        SearchSuggestionResponse response2 = new SearchSuggestionResponse(suggestions);

        // Act & Assert
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }
}
