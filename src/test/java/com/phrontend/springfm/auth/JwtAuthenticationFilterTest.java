package com.phrontend.springfm.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_WithValidToken_SetsAuthentication() throws ServletException, IOException {
        // Arrange
        String token = "valid-jwt-token";
        request.addHeader("Authorization", "Bearer " + token);

        Claims claims = Jwts.claims()
                .subject("1")
                .add("email", "test@example.com")
                .add("canUpload", true)
                .build();

        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.parseToken(token)).thenReturn(claims);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo("1");
        assertThat(authentication.getAuthorities()).hasSize(1);
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .contains("UPLOAD");

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithValidTokenAndNoUploadPermission_SetsAuthenticationWithoutUploadAuthority() throws ServletException, IOException {
        // Arrange
        String token = "valid-jwt-token";
        request.addHeader("Authorization", "Bearer " + token);

        Claims claims = Jwts.claims()
                .subject("2")
                .add("email", "user@example.com")
                .add("canUpload", false)
                .build();

        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.parseToken(token)).thenReturn(claims);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo("2");
        assertThat(authentication.getAuthorities()).isEmpty();

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithInvalidToken_DoesNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        String token = "invalid-jwt-token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.validateToken(token)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(jwtService, never()).parseToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithoutAuthorizationHeader_DoesNotSetAuthentication() throws ServletException, IOException {
        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(jwtService, never()).validateToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithNonBearerToken_DoesNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(jwtService, never()).validateToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithEmptyBearerToken_DoesNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "Bearer ");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(jwtService).validateToken("");
        verify(filterChain).doFilter(request, response);
    }
}
