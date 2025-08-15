package com.example.api_sistema_ventas.security;

import java.util.Base64;
import java.util.Date;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtTokenProvider {
    private static final String SECRET_KEY = "nRfUQ/9NyXooocdK97b0ovRZ011PYjuW657tRjY4M+fkme/JpX6/AMNxP6IHCgeNsKiJJ8lC4OF9DIQDcEySMQ==";
    private static final long EXPIRATION_TIME = 86400000; // 1 day

    private final byte[] decodedKey = Base64.getDecoder().decode(SECRET_KEY);

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, decodedKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(decodedKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(decodedKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}