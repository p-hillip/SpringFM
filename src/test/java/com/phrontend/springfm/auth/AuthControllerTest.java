package com.phrontend.springfm.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void register_WithValidData_ReturnsAuthResponse() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "newuser@example.com",
                "password123",
                "New User"
        );
        UserResponse userResponse = new UserResponse("1", "newuser@example.com", "New User", false);
        AuthResponse authResponse = new AuthResponse("jwt-token", userResponse);

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        // Act
        AuthResponse response = authController.register(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo("newuser@example.com");
        assertThat(response.user().name()).isEqualTo("New User");
        assertThat(response.user().canUpload()).isFalse();

        verify(authService).register(request);
    }

    @Test
    void login_WithValidCredentials_ReturnsAuthResponse() {
        // Arrange
        LoginRequest request = new LoginRequest("user@example.com", "password123", false);
        UserResponse userResponse = new UserResponse("1", "user@example.com", "Test User", true);
        AuthResponse authResponse = new AuthResponse("jwt-token", userResponse);

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        // Act
        AuthResponse response = authController.login(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo("user@example.com");
        assertThat(response.user().canUpload()).isTrue();

        verify(authService).login(request);
    }

    @Test
    void logout_ReturnsOk() {
        // Act
        ResponseEntity<Void> response = authController.logout();

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void me_ReturnsUserEnvelope() {
        // Arrange
        String userId = "1";
        UserResponse userResponse = new UserResponse("1", "user@example.com", "Test User", false);

        when(authService.currentUser(userId)).thenReturn(userResponse);

        // Act
        UserEnvelope envelope = authController.me(userId);

        // Assert
        assertThat(envelope).isNotNull();
        assertThat(envelope.user()).isEqualTo(userResponse);

        verify(authService).currentUser(userId);
    }
}
