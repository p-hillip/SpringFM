package com.phrontend.springfm.auth;

public record UserResponse(
        String id,
        String email,
        String name,
        boolean canUpload
) {
}
