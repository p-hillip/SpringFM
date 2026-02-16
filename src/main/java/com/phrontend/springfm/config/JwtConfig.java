package com.phrontend.springfm.config;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Bean
    public SecretKey jwtSecretKey(JwtProperties properties) {
        return Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    @Bean
    public JwtParser jwtParser(SecretKey jwtSecretKey, JwtProperties properties) {
        return Jwts.parser()
                .verifyWith(jwtSecretKey)
                .requireIssuer(properties.issuer())
                .build();
    }
}
