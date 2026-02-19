package com.phrontend.springfm.auth;

import com.phrontend.springfm.config.JwtProperties;
import com.phrontend.springfm.user.UserEntity;
import com.phrontend.springfm.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .id(1L)
                .email("test@example.com")
                .displayName("Test User")
                .passwordHash("hashedPassword")
                .canUpload(false)
                .build();
    }

    @Test
    void login_WithValidCredentials_ReturnsAuthResponse() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "password123", false);
        Authentication authentication = mock(Authentication.class);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByEmailIgnoreCase("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(jwtProperties.accessTokenTtl()).thenReturn(Duration.ofHours(1));
        when(jwtService.generateToken(any(UserEntity.class), any(Duration.class)))
                .thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo("test@example.com");
        assertThat(response.user().name()).isEqualTo("Test User");
        assertThat(response.user().canUpload()).isFalse();
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(testUser, Duration.ofHours(1));
    }

    @Test
    void login_WithRememberMe_UsesRememberMeTtl() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "password123", true);
        Authentication authentication = mock(Authentication.class);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByEmailIgnoreCase("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(jwtProperties.rememberMeTtl()).thenReturn(Duration.ofDays(7));
        when(jwtService.generateToken(any(UserEntity.class), any(Duration.class)))
                .thenReturn("jwt-token");

        // Act
        authService.login(request);

        // Assert
        verify(jwtProperties).rememberMeTtl();
        verify(jwtService).generateToken(testUser, Duration.ofDays(7));
    }

    @Test
    void login_WithInvalidCredentials_ThrowsException() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "wrongpassword", false);
        Authentication authentication = mock(Authentication.class);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Authentication failed");
    }

    @Test
    void register_WithValidData_CreatesUserAndReturnsAuthResponse() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "newuser@example.com",
                "password123",
                "New User"
        );
        UserEntity savedUser = UserEntity.builder()
                .id(2L)
                .email("newuser@example.com")
                .displayName("New User")
                .passwordHash("hashedPassword")
                .canUpload(false)
                .build();

        when(userRepository.findByEmailIgnoreCase("newuser@example.com"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123"))
                .thenReturn("hashedPassword");
        when(userRepository.save(any(UserEntity.class)))
                .thenReturn(savedUser);
        when(jwtProperties.accessTokenTtl()).thenReturn(Duration.ofHours(1));
        when(jwtService.generateToken(any(UserEntity.class), any(Duration.class)))
                .thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo("newuser@example.com");
        assertThat(response.user().name()).isEqualTo("New User");
        assertThat(response.user().canUpload()).isFalse();

        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void register_WithExistingEmail_ThrowsException() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "existing@example.com",
                "password123",
                "Existing User"
        );

        when(userRepository.findByEmailIgnoreCase("existing@example.com"))
                .thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already registered");

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void currentUser_WithValidUserId_ReturnsUserResponse() {
        // Arrange
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testUser));

        // Act
        UserResponse response = authService.currentUser("1");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo("1");
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.name()).isEqualTo("Test User");
        assertThat(response.canUpload()).isFalse();
    }

    @Test
    void currentUser_WithInvalidUserId_ThrowsException() {
        // Arrange
        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.currentUser("999"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");
    }
}
