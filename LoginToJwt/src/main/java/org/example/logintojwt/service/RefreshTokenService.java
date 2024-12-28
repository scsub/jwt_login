package org.example.logintojwt.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.logintojwt.config.security.CustomUserDetailsService;
import org.example.logintojwt.entity.RefreshToken;
import org.example.logintojwt.properties.JwtTokenProperties;
import org.example.logintojwt.request.UserAndTokenRequest;
import org.example.logintojwt.jwt.JwtProvider;
import org.example.logintojwt.repository.RefreshTokenRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtTokenProperties jwtTokenProperties;

    public void saveRefreshToken(String username, String token, Long expiration) {
        Optional<RefreshToken> checkToken = refreshTokenRepository.findByUsername(username);

        RefreshToken refreshToken;
        if (checkToken.isPresent()) {
            refreshToken = checkToken.get();
            refreshToken.changeToken(token);
            refreshToken.changeExpiration(expiration);
        } else {
            refreshToken = RefreshToken.builder()
                    .username(username)
                    .token(token)
                    .expiration(expiration)
                    .build();
        }
        refreshTokenRepository.save(refreshToken);
    }

    public UserAndTokenRequest accessTokenReissue(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getRefreshToken(request);
        if (refreshToken != null && jwtProvider.validateToken(refreshToken)) {
            String username = jwtProvider.getUsername(refreshToken);
            if (validateRefreshToken(username, refreshToken)) {
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                // 이미 인증된 토큰을 가지고 있으므로 자격 증명이 필요없다
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                String accessToken = jwtProvider.createAccessToken(authenticationToken);

                ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", accessToken)
                        .httpOnly(true)
                        .secure(true)
                        .path("/")
                        .maxAge(jwtTokenProperties.getAccessValidTime())
                        .sameSite("None")
                        .build();
                response.setHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());

                UserAndTokenRequest userAndTokenRequest = UserAndTokenRequest.builder()
                        .username(username)
                        .token(accessToken)
                        .build();

                return userAndTokenRequest;
            } else {
                throw new BadCredentialsException("리프레시 토큰이 유효하지 않음");
            }
        } else {
            throw new BadCredentialsException("리프레시 토큰이 만료되었거나 유효하지 않음");
        }
    }

    private boolean validateRefreshToken(String username, String token) {
        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findByUsername(username);
        if (optionalRefreshToken.isPresent()) {
            RefreshToken refreshToken = optionalRefreshToken.get();

            return refreshToken.getToken().equals(token) &&
                    refreshToken.getExpiration() > Instant.now().getEpochSecond();
        }
        return false;
    }



    public void deleteRefreshToken(String username) {
        refreshTokenRepository.deleteByUsername(username);
    }

    public String getRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName()) && cookie.getValue() != null) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
