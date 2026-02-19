package com.phrontend.springfm.auth;

import com.phrontend.springfm.config.JwtProperties;
import com.phrontend.springfm.user.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    @SuppressWarnings("FieldCanBeLocal")
    private SecretKey secretKey;
    @SuppressWarnings("FieldCanBeLocal")
    private JwtParser jwtParser;
    @SuppressWarnings("FieldCanBeLocal")
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        String secret = "test-secret-key-that-is-long-enough-for-hmac-sha256-algorithm";
        secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        
        jwtProperties = new JwtProperties(
                secret,
                "test-issuer",
                Duration.ofHours(1),
                Duration.ofDays(7)
        );
        
        jwtParser = Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer("test-issuer")
                .build();
        
        jwtService = new JwtService(secretKey, jwtParser, jwtProperties);
    }

    @Test
    void generateToken_CreatesValidJwt() {
        // Arrange
        UserEntity user = UserEntity.builder()
                .id(1L)
                .email("test@example.com")
                .displayName("Test User")
                .canUpload(true)
                .build();

        // Act
        String token = jwtService.generateToken(user, Duration.ofHours(1));

        // Assert
        assertThat(token).isNotNull().isNotEmpty();
        
        Claims claims = jwtService.parseToken(token);
        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("email", String.class)).isEqualTo("test@example.com");
        assertThat(claims.get("name", String.class)).isEqualTo("Test User");
        assertThat(claims.get("canUpload", Boolean.class)).isTrue();
        assertThat(claims.getIssuer()).isEqualTo("test-issuer");
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
    }

    @Test
    void generateToken_WithCanUploadFalse_IncludesCorrectClaim() {
        // Arrange
        UserEntity user = UserEntity.builder()
                .id(2L)
                .email("user@example.com")
                .displayName("Regular User")
                .canUpload(false)
                .build();

        // Act
        String token = jwtService.generateToken(user, Duration.ofMinutes(30));

        // Assert
        Claims claims = jwtService.parseToken(token);
        assertThat(claims.get("canUpload", Boolean.class)).isFalse();
    }

    @Test
    void parseToken_WithValidToken_ReturnsCorrectClaims() {
        // Arrange
        UserEntity user = UserEntity.builder()
                .id(3L)
                .email("parse@example.com")
                .displayName("Parse Test")
                .canUpload(true)
                .build();
        String token = jwtService.generateToken(user, Duration.ofHours(2));

        // Act
        Claims claims = jwtService.parseToken(token);

        // Assert
        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo("3");
        assertThat(claims.get("email", String.class)).isEqualTo("parse@example.com");
        assertThat(claims.get("name", String.class)).isEqualTo("Parse Test");
    }

    @Test
    void validateToken_WithValidToken_ReturnsTrue() {
        // Arrange
        UserEntity user = UserEntity.builder()
                .id(4L)
                .email("valid@example.com")
                .displayName("Valid User")
                .canUpload(false)
                .build();
        String token = jwtService.generateToken(user, Duration.ofHours(1));

        // Act
        boolean isValid = jwtService.validateToken(token);

        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_WithInvalidToken_ReturnsFalse() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act
        boolean isValid = jwtService.validateToken(invalidToken);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_WithExpiredToken_ReturnsFalse() {
        // Arrange
        UserEntity user = UserEntity.builder()
                .id(5L)
                .email("expired@example.com")
                .displayName("Expired User")
                .canUpload(false)
                .build();
        
        // Generate token with negative duration (already expired)
        String token = jwtService.generateToken(user, Duration.ofSeconds(-1));

        // Act
        boolean isValid = jwtService.validateToken(token);

        // Assert
        assertThat(isValid).isFalse();
    }
}
