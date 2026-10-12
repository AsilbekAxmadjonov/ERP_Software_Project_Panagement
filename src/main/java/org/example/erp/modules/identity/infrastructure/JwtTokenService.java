package org.example.erp.modules.identity.infrastructure;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.erp.modules.identity.application.contracts.TokenService;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

class JwtTokenService implements TokenService {
    private final SecretKey key;
    private final long ttlMs;

    JwtTokenService(String secret, long ttlHours) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttlMs = ttlHours * 3_600_000L;
    }

    @Override
    public String issue(String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder().subject(username).issuedAt(new Date(now)).expiration(new Date(now + ttlMs)).signWith(key).compact();
    }

    @Override
    public Optional<String> usernameFrom(String token) {
        try {
            return Optional.of(Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
