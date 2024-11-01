package org.example.logintojwt.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.logintojwt.entity.User;
import org.example.logintojwt.entity.UserAndTokenRequest;
import org.example.logintojwt.entity.UserRequest;
import org.example.logintojwt.entity.UserResponse;
import org.example.logintojwt.exception.UserAlreadyExistsException;
import org.example.logintojwt.jwt.JwtProvider;
import org.example.logintojwt.repository.UserRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailsService customUserDetailsService;

    public UserResponse signup(UserRequest userRequest) {
        String username = userRequest.getUsername();
        String password = userRequest.getPassword();
        userRepository.findByUsername(username)
                .ifPresent(user -> {
                    throw new UserAlreadyExistsException("이미 존재하는 아이디입니다.");
                });

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .build(); // 기본 roles값은 USER

        userRepository.save(user);
        return new UserResponse(username, "회원 가입 성공");
    }

    public UserAndTokenRequest login(UserRequest userRequest, HttpServletResponse response) {
        String username = userRequest.getUsername();
        String password = userRequest.getPassword();
        try {
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            Authentication authenticate = authenticationManager.authenticate(usernamePasswordAuthenticationToken);

            // 토큰발급
            String accessToken = jwtProvider.createAccessToken(authenticate);
            String refreshToken = jwtProvider.createRefreshToken(authenticate);

            // 왜 굳이 DB에도 만료시간을 저장하는가
            // 토큰을 파싱하지 않아도 만료여부를 빠르게 알수있다
            // DB에서 만료시간을 아는것보다 파싱하는것이 비용이 더 높다
            // (현재시간 + 리프레시 토큰의 만료시간)를  (Unix 타임스탬프)초 단위로 변환
            long refreshTokenExpiration = Instant.now().plusMillis(jwtProvider.getRefreshTokenValidTime()).getEpochSecond();
            //리프레시 토큰의 만료기간을 알아온후 그 토큰을 저장했을뿐
            refreshTokenService.saveRefreshToken(username, refreshToken, refreshTokenExpiration);


            // https를 사용해야하는데 사용법을 모르겠음
            ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", accessToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(13) // 이 쿠키를 브라우저에 보관하는시간 시간이 지나면 사라짐 만일 지우지 않으면
                    //브라우저를 닫을때 까지 유지된다
                    // 또한 토큰이 만료되도 계속해서 서버로 토큰이 전송된다
                    .sameSite("Lax")
                    .build();

            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(false) // true면 https로만 접근가능하다 하지만 로컬 개발을 하고있으니 일단 사용한다
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60) // 7일
                    .sameSite("Lax") //Strict는 크로스요청에 대해 전송되지 않는다, Lax나 None으로 설정해야하나 None으로 할경우 Secure=true여야 한다
                    .build();


            response.setHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

            UserAndTokenRequest userAndTokenRequest = UserAndTokenRequest.builder()
                    .username(accessToken)
                    .token(refreshToken)
                    .build();

            return userAndTokenRequest;

        } catch (AuthenticationException e) {
            throw new BadCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다.", e);
        }

    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {

    }

    public UserAndTokenRequest accessTokenReissue(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getRefreshToken(request);
        if (refreshToken != null && jwtProvider.validateToken(refreshToken)) {
            String username = jwtProvider.getUsername(refreshToken);

            if (refreshTokenService.validateRefreshToken(username, refreshToken)) {
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                String accessToken = jwtProvider.createAccessToken(authenticationToken);

                ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", accessToken)
                        .httpOnly(true)
                        .secure(false)
                        .path("/")
                        .maxAge(13)
                        .sameSite("Lax")
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

    private String getRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
