package com.phrontend.springfm.auth;

public record AuthResponse(
        String token,
        UserResponse user
) {
}
