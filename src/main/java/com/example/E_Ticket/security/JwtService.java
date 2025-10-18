package com.example.E_Ticket.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtService {
    private final SecretKey key;
    private final long expSeconds;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.exp-seconds}") long expSeconds) {
        // secret >= 32 bytes cho HS256
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expSeconds = expSeconds;
    }

    public String generate(String username, Collection<?> authorities){
        String roles = authorities.stream().map(Object::toString).collect(Collectors.joining(","));
        Date now = new Date();
        Date exp = new Date(now.getTime() + expSeconds * 1000);

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    public Jws<Claims> parse(String token){
        return Jwts.parser()
                .verifyWith(key)   // ← nhận SecretKey
                .build()
                .parseSignedClaims(token);
    }
}
