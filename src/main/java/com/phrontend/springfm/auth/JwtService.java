package com.phrontend.springfm.auth;

import com.phrontend.springfm.config.JwtProperties;
import com.phrontend.springfm.user.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final SecretKey jwtSecretKey;
    private final JwtParser jwtParser;
    private final JwtProperties jwtProperties;

    public String generateToken(UserEntity user, Duration ttl) {
        Instant now = Instant.now();
        Instant expiration = now.plus(ttl);

        return Jwts.builder()
                .issuer(jwtProperties.issuer())
                .subject(user.getId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .claim("email", user.getEmail())
                .claim("name", user.getDisplayName())
                .claim("canUpload", user.getCanUpload())
                .signWith(jwtSecretKey)
                .compact();
    }

    public Claims parseToken(String token) {
        return jwtParser.parseSignedClaims(token).getPayload();
    }

    public boolean validateToken(String token) {
        try {
            jwtParser.parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
