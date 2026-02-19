package com.phrontend.springfm.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler exceptionHandler = new ApiExceptionHandler();

    @Test
    void handleIllegalArgument_ReturnsNotFoundWithErrorMessage() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Resource not found");

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleIllegalArgument(exception);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry("error", "Resource not found");
    }

    @Test
    void handleIllegalArgument_WithDifferentMessage_ReturnsCorrectMessage() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("User not found");

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleIllegalArgument(exception);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("error", "User not found");
    }

    @Test
    void handleIllegalArgument_WithEmptyMessage_ReturnsEmptyMessage() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("");

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleIllegalArgument(exception);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("error", "");
    }
}
