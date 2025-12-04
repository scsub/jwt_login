package org.example.logintojwt.service.unit;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.logintojwt.config.security.CustomUserDetailsService;
import org.example.logintojwt.entity.RefreshToken;
import org.example.logintojwt.exception.InvalidRefreshTokenException;
import org.example.logintojwt.config.security.JwtProvider;
import org.example.logintojwt.properties.JwtTokenProperties;
import org.example.logintojwt.repository.RefreshTokenRepository;
import org.example.logintojwt.request.RefreshTokenRequest;
import org.example.logintojwt.request.UserAndAccessTokenRequest;
import org.example.logintojwt.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceUnitTest {
    @InjectMocks
    private RefreshTokenService refreshTokenService;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private CustomUserDetailsService customUserDetailsService;
    @Mock
    private JwtTokenProperties jwtTokenProperties;
    @Mock
    private UserDetails userDetails;
    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private HttpServletResponse httpServletResponse;
    private RefreshToken refreshToken;
    private RefreshTokenRequest refreshTokenRequest;
    private RefreshTokenRequest newRefreshTokenRequest;

    @BeforeEach
    void setUp() {
        refreshToken = RefreshToken.builder()
                .username("kimone")
                .token("refreshToken")
                .expiration(Instant.now().plusSeconds(604800L).getEpochSecond())
                .build();

        refreshTokenRequest = RefreshTokenRequest.builder()
                .username("kimone")
                .token("requestRefreshToken")
                .expiration(Instant.now().plusSeconds(604800L).getEpochSecond())
                .build();
        newRefreshTokenRequest = RefreshTokenRequest.builder()
                .username("kimone")
                .token("newRequestRefreshToken")
                .expiration(Instant.now().plusSeconds(604800L).getEpochSecond())
                .build();

    }

    @Test
    @DisplayName("첫번쨰 로그인: 리프레시 토큰 저장 성공")
    void createRefreshToken() {
        when(refreshTokenRepository.findByUsername(refreshTokenRequest.getUsername())).thenReturn(Optional.empty());
        //
        refreshTokenService.saveRefreshToken(refreshTokenRequest);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        RefreshToken savedRefreshToken = captor.getValue();
        assertThat(savedRefreshToken.getUsername()).isEqualTo(refreshTokenRequest.getUsername());
        assertThat(savedRefreshToken.getToken()).isEqualTo(refreshTokenRequest.getToken());
    }

    @Test
    @DisplayName("다음 로그인: 리프레시 토큰 재발급")
    void refreshTokenReissue() {
        when(refreshTokenRepository.findByUsername(refreshTokenRequest.getUsername())).thenReturn(Optional.of(refreshToken));
        refreshTokenService.saveRefreshToken(newRefreshTokenRequest);

        verify(refreshTokenRepository).save(any(RefreshToken.class));
        assertThat(refreshToken.getUsername()).isEqualTo(newRefreshTokenRequest.getUsername());
        assertThat(refreshToken.getToken()).isEqualTo(newRefreshTokenRequest.getToken());
    }


    @Test
    @DisplayName("예외 : 유효하지 않은 리프레시 토큰")
    void cantAccessTokenReissue() {
        String wrongRefreshToken = "wrongRefreshToken";
        when(httpServletRequest.getCookies()).thenReturn(new Cookie[]{new Cookie("refreshToken", wrongRefreshToken)});
        when(jwtProvider.validateToken(anyString())).thenReturn(false);

        assertThatThrownBy(() -> refreshTokenService.accessTokenReissue(httpServletRequest, httpServletResponse))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessage("리프레시 토큰이 만료되었거나 유효하지 않음");
    }

    @Test
    @DisplayName("엑세스 토큰 발급 성공")
    void accessTokenReissueSuccess() {
        String username = "username";
        String validRefreshToken = "refreshToken";
        String newAccessToken = "newAccessToken";
        when(httpServletRequest.getCookies()).thenReturn(new Cookie[]{new Cookie("refreshToken", validRefreshToken)});

        when(jwtProvider.validateToken(validRefreshToken)).thenReturn(true);
        when(jwtProvider.getUsername(validRefreshToken)).thenReturn(username);

        when(refreshTokenRepository.findByUsername(username)).thenReturn(Optional.of(refreshToken));


        when(jwtProvider.createAccessToken(any())).thenReturn(newAccessToken);
        when(jwtTokenProperties.getAccessValidTime()).thenReturn(3600L);

        when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());
        when(customUserDetailsService.loadUserByUsername(username)).thenReturn(userDetails);


        UserAndAccessTokenRequest userAndAccessTokenRequest = refreshTokenService.accessTokenReissue(httpServletRequest, httpServletResponse);

        assertThat(userAndAccessTokenRequest.getUsername()).isEqualTo(username);
        assertThat(userAndAccessTokenRequest.getToken()).isEqualTo(newAccessToken);

    }
}