package fpt.tuanhm43.server.services.impl;

import fpt.tuanhm43.server.services.TokenService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private static final Logger log = LoggerFactory.getLogger(TokenServiceImpl.class);

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.access-expiration}")
    private long accessExpiration;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpiration;

    private static final String AUTHORITIES_KEY = "auth";
    private final StringRedisTemplate redisTemplate;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String generateAccessToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public String generateRefreshToken(Authentication authentication) {
        return Jwts.builder()
                .subject(authentication.getName())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        }
        return false;
    }

    @Override
    public Authentication getAuthenticationFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Object authoritiesClaim = claims.get(AUTHORITIES_KEY);
        if (authoritiesClaim == null) {
            log.warn("Token has no authorities claim");
            return null;
        }

        List<SimpleGrantedAuthority> authorities =
                Arrays.stream(authoritiesClaim.toString().split(","))
                        .filter(auth -> !auth.trim().isEmpty())
                        .map(SimpleGrantedAuthority::new)
                        .toList();

        User principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    @Override
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    @Override
    public Long getExpirationTime(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .getTime();
    }

    public void blacklistToken(String token, long expirationTimeInMillis) {
        String key = "blacklist:" + token;
        long ttl = expirationTimeInMillis - System.currentTimeMillis();

        if (ttl > 0) {
            redisTemplate.opsForValue().set(key, "true", Duration.ofMillis(ttl));
        }
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token));
    }
}