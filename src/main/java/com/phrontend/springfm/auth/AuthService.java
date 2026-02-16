package com.phrontend.springfm.auth;

import com.phrontend.springfm.config.JwtProperties;
import com.phrontend.springfm.user.UserEntity;
import com.phrontend.springfm.user.UserRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        if (!authentication.isAuthenticated()) {
            throw new IllegalStateException("Authentication failed");
        }

        UserEntity user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Duration ttl = request.remember() ? jwtProperties.rememberMeTtl() : jwtProperties.accessTokenTtl();
        String token = jwtService.generateToken(user, ttl);
        return new AuthResponse(token, toUserResponse(user));
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser(String userId) {
        Long id = Long.parseLong(userId);
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return toUserResponse(user);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmailIgnoreCase(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        UserEntity user = UserEntity.builder()
                .email(request.email())
                .displayName(request.displayName())
                .passwordHash(passwordEncoder.encode(request.password()))
                .canUpload(false)
                .build();

        user = userRepository.save(user);

        Duration ttl = jwtProperties.accessTokenTtl();
        String token = jwtService.generateToken(user, ttl);
        return new AuthResponse(token, toUserResponse(user));
    }

    private UserResponse toUserResponse(UserEntity user) {
        return new UserResponse(
                user.getId().toString(),
                user.getEmail(),
                user.getDisplayName(),
                user.getCanUpload()
        );
    }
}
