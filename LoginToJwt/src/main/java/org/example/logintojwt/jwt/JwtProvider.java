package org.example.logintojwt.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtProvider {
    @Value("${jwt.secret}")
    private String secretKey;
    private Key key;
    private final long validTime = 1000L; //1초
    private final long refreshTokenValidTime = 60 * 60 * 24 * 7;
    private final long accessTokenValidTime = 13;
    @PostConstruct
    protected void init() {

        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String createAccessToken(Authentication authentication) {
        String username = authentication.getName();
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roles", roles);

        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessTokenValidTime);

        Date issued = Date.from(now);
        Date expiration = Date.from(exp);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(issued)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(Authentication authentication) {
        String username = authentication.getName();
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roles", roles);

        Instant now = Instant.now();
        Instant exp = now.plusSeconds(refreshTokenValidTime); // 초 단위로 시간 추가

        Date issued = Date.from(now);
        Date expiration = Date.from(exp);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(issued)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return true;

        } catch (ExpiredJwtException e) {
            throw e; // 예외 던져서 Filter에서 예외 발생시킬것임
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public long getRefreshTokenValidTime() {
        return refreshTokenValidTime;
    }
}
