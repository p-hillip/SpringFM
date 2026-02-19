package com.phrontend.springfm.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .id(1L)
                .email("test@example.com")
                .displayName("Test User")
                .passwordHash("hashedPassword123")
                .canUpload(true)
                .build();
    }

    @Test
    void loadUserByUsername_WithExistingUser_ReturnsUserDetails() {
        // Arrange
        when(userRepository.findByEmailIgnoreCase("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userService.loadUserByUsername("test@example.com");

        // Assert
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("test@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("hashedPassword123");
        assertThat(userDetails.getAuthorities()).hasSize(2);
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .contains("ROLE_USER", "UPLOAD");
    }

    @Test
    void loadUserByUsername_WithUserWithoutUploadPermission_ReturnsUserDetailsWithoutUploadAuthority() {
        // Arrange
        UserEntity userWithoutUpload = UserEntity.builder()
                .id(2L)
                .email("regular@example.com")
                .displayName("Regular User")
                .passwordHash("hashedPassword456")
                .canUpload(false)
                .build();

        when(userRepository.findByEmailIgnoreCase("regular@example.com"))
                .thenReturn(Optional.of(userWithoutUpload));

        // Act
        UserDetails userDetails = userService.loadUserByUsername("regular@example.com");

        // Assert
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .contains("ROLE_USER")
                .doesNotContain("UPLOAD");
    }

    @Test
    void loadUserByUsername_WithNonExistentUser_ThrowsUsernameNotFoundException() {
        // Arrange
        when(userRepository.findByEmailIgnoreCase("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.loadUserByUsername("nonexistent@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void loadUserByUsername_IsCaseInsensitive() {
        // Arrange
        when(userRepository.findByEmailIgnoreCase("TEST@EXAMPLE.COM"))
                .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userService.loadUserByUsername("TEST@EXAMPLE.COM");

        // Assert
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("test@example.com");
    }

    @Test
    void requireById_WithExistingId_ReturnsUser() {
        // Arrange
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testUser));

        // Act
        UserEntity user = userService.requireById(1L);

        // Assert
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getDisplayName()).isEqualTo("Test User");
    }

    @Test
    void requireById_WithNonExistentId_ThrowsUsernameNotFoundException() {
        // Arrange
        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.requireById(999L))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");
    }
}
