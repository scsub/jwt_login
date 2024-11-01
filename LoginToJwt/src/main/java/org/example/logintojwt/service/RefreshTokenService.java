package org.example.logintojwt.service;

import lombok.RequiredArgsConstructor;
import org.example.logintojwt.entity.RefreshToken;
import org.example.logintojwt.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

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

    public boolean validateRefreshToken(String username, String token) {
        Optional<RefreshToken> obj = refreshTokenRepository.findByUsername(username);

        if (obj.isPresent()) {
            RefreshToken refreshToken = obj.get();
            return refreshToken.getToken().equals(token) &&
                    refreshToken.getExpiration() > Instant.now().getEpochSecond();
        }
        return false;
    }

    public void deleteRefreshToken(String username) {
        refreshTokenRepository.deleteById(username);
    }
}
