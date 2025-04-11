package org.example.logintojwt.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.logintojwt.entity.Cart;
import org.example.logintojwt.entity.User;
import org.example.logintojwt.properties.JwtTokenProperties;
import org.example.logintojwt.repository.CartRepository;
import org.example.logintojwt.request.RefreshTokenRequest;
import org.example.logintojwt.request.UserLoginRequest;
import org.example.logintojwt.request.UserProfileRequest;
import org.example.logintojwt.request.UserRegistrationRequest;
import org.example.logintojwt.response.LoginSuccessResponse;
import org.example.logintojwt.response.UserResponse;
import org.example.logintojwt.exception.UserAlreadyExistsException;
import org.example.logintojwt.config.security.JwtProvider;
import org.example.logintojwt.repository.UserRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    private final CartRepository cartRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProperties jwtTokenProperties;

    public UserResponse signup(UserRegistrationRequest userRegistrationRequest) {
        String username = userRegistrationRequest.getUsername();
        String password = userRegistrationRequest.getPassword();
        String email = userRegistrationRequest.getEmail();
        String phoneNumber = userRegistrationRequest.getPhoneNumber();
        String address = userRegistrationRequest.getAddress();
        if (userRepository.findByUsername(username).isPresent()){
            throw new UserAlreadyExistsException("이미 존재하는 아이디입니다");
        }
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .phoneNumber(phoneNumber)
                .address(address)
                .build();

        userRepository.save(user);

        // 카트 생성
        Cart cart = Cart.builder()
                .user(user)
                .build();
        cartRepository.save(cart);
        user.assignCart(cart);

        return new UserResponse(username, "회원 가입 성공");
    }

    public LoginSuccessResponse login(UserLoginRequest userLoginRequest, HttpServletResponse response) {
        String username = userLoginRequest.getUsername();
        String password = userLoginRequest.getPassword();

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authenticate = authenticationManager.authenticate(usernamePasswordAuthenticationToken);

        // 엑세스, 리프래시 토큰발급
        String accessToken = jwtProvider.createAccessToken(authenticate);
        String refreshToken = jwtProvider.createRefreshToken(authenticate);

        // 리프레시 토큰 저장
        RefreshTokenRequest refreshTokenRequest = RefreshTokenRequest.builder()
                .username(username)
                .token(refreshToken)
                .expiration(getRefreshTokenExpiration())
                .build();
        refreshTokenService.saveRefreshToken(refreshTokenRequest);

        // httponly 쿠키 발급
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(jwtTokenProperties.getAccessValidTime())
                .sameSite("None")
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(jwtTokenProperties.getRefreshValidTime()) // 7일
                .sameSite("None")
                .build();


        // 헤더에 쿠키를 저장
        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        return LoginSuccessResponse.builder()
                .message("로그인 성공")
                .build();
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
                .secure(true) // https 시 true
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();
        log.info("엑세스 토큰 : {}", accessTokenCookie);
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", null)
                .httpOnly(true)
                .secure(true) // https 시 true
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();
        log.info("리프레시 토큰 : {}", refreshTokenCookie);

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        log.info("토큰을 헤더에 삽입");
        return "로그아웃 됨";
    }

    public void changeProfile(UserDetails userDetails, UserProfileRequest userProfileRequest) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new UsernameNotFoundException("유저를 발견하지 못함"));
        user.updateProfile(passwordEncoder.encode(userProfileRequest.getPassword()), userProfileRequest.getEmail(), userProfileRequest.getPhoneNumber(), userProfileRequest.getAddress());
        userRepository.save(user);
    }

    public void deleteUser(UserDetails userDetails) {
        String username = userDetails.getUsername();
        userRepository.deleteByUsername(username);
        refreshTokenService.deleteRefreshToken(username);
    }

    private long getRefreshTokenExpiration(){
        return Instant.now().plusSeconds(jwtTokenProperties.getRefreshValidTime()).getEpochSecond();
    }

}

