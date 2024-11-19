package org.example.logintojwt.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.logintojwt.entity.User;
import org.example.logintojwt.entity.request.UserAndTokenRequest;
import org.example.logintojwt.entity.request.UserRequest;
import org.example.logintojwt.entity.response.AccessTokenAndRefreshTokenResponse;
import org.example.logintojwt.entity.response.UserResponse;
import org.example.logintojwt.exception.UserAlreadyExistsException;
import org.example.logintojwt.jwt.JwtProvider;
import org.example.logintojwt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailsService customUserDetailsService;

    @Value("${refresh-valid-time}")
    private long refreshTokenValidTime;

    @Value("${access-valid-time}")
    private long accessTokenValidTime;

    public UserResponse signup(UserRequest userRequest) {
        String username = userRequest.getUsername();
        String password = userRequest.getPassword();
        if (userRepository.findByUsername(username).isPresent()){
            throw new UserAlreadyExistsException("이미 존재하는 아이디입니다");
        }
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .build(); // 기본 roles값은 USER

        userRepository.save(user);
        return new UserResponse(username, "회원 가입 성공");
    }

    public AccessTokenAndRefreshTokenResponse login(UserRequest userRequest, HttpServletResponse response) {
        String username = userRequest.getUsername();
        String password = userRequest.getPassword();
        try {
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            Authentication authenticate = authenticationManager.authenticate(usernamePasswordAuthenticationToken);

            // 엑세스, 리프래시 토큰발급
            String accessToken = jwtProvider.createAccessToken(authenticate);
            String refreshToken = jwtProvider.createRefreshToken(authenticate);

            // 리프레시 토큰 저장
            refreshTokenService.saveRefreshToken(username, refreshToken, getRefreshTokenExpiration());

            ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", accessToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(accessTokenValidTime)
                    .sameSite("Lax")
                    .build();

            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(refreshTokenValidTime) // 7일
                    .sameSite("Lax")
                    .build();


            response.setHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

            AccessTokenAndRefreshTokenResponse accessTokenAndRefreshTokenResponse = AccessTokenAndRefreshTokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
            return accessTokenAndRefreshTokenResponse;

        } catch (AuthenticationException e) {
            throw new BadCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다.", e);
        }

    }

    

    public String logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("로그아웃 시작");
        String refreshToken = refreshTokenService.getRefreshToken(request);
        log.info("토큰 : {}", refreshToken);
        String username = jwtProvider.getUsername(refreshToken);
        log.info("유저 : {}", username);
        refreshTokenService.deleteRefreshToken(username);
        log.info("삭제완료");

        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", null)
                .httpOnly(true)
                .secure(false) // https 시 true
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        log.info("엑세스 토큰 : {}", accessTokenCookie);
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", null)
                .httpOnly(true)
                .secure(false) // https 시 true
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        log.info("리프레시 토큰 : {}", refreshTokenCookie);

        response.setHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        log.info("토큰을 헤더에 삽입");
        return "로그아웃 됨";
    }
    private long getRefreshTokenExpiration(){
        // (현재시간 + 리프레시 토큰의 만료시간)를  (Unix 타임스탬프)초 단위로 변환
        return Instant.now().plusMillis(refreshTokenValidTime).getEpochSecond();
    }

}
