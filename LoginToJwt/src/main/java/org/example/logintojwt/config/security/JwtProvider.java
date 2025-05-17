package org.example.logintojwt.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.logintojwt.properties.JwtTokenProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {
    private final JwtTokenProperties jwtTokenProperties;
    private Key key;


    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(jwtTokenProperties.getSecret().getBytes());
    }

    public String createAccessToken(Authentication authentication) {
        return createToken(authentication, jwtTokenProperties.getAccessValidTime());
    }

    public String createRefreshToken(Authentication authentication) {
        return createToken(authentication, jwtTokenProperties.getRefreshValidTime());
    }

    private String createToken(Authentication authentication, long tokenValidTime) {
        String username = getAuthUsername(authentication);
        List<String> roles = getAuthRoles(authentication);

        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roles", roles);

        Instant now = Instant.now();
        Instant exp = now.plusSeconds(tokenValidTime);

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
        log.info("필터 안에 들어옴");
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return true;

        } catch (ExpiredJwtException e) { // 만료됐으면 예외 던짐
            throw e;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    public List<String> getRoles(String token) {
         return getClaims(token).get("roles", List.class);
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String getAuthUsername(Authentication authentication) {
        return authentication.getName();
    }

    private List<String> getAuthRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }
}
