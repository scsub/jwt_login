package org.example.logintojwt.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.logintojwt.entity.request.UserAndTokenRequest;
import org.example.logintojwt.entity.request.UserRequest;
import org.example.logintojwt.entity.response.AccessTokenAndRefreshTokenResponse;
import org.example.logintojwt.entity.response.UserResponse;
import org.example.logintojwt.service.RefreshTokenService;
import org.example.logintojwt.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody UserRequest userRequest) {
        return ResponseEntity.ok(userService.signup(userRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AccessTokenAndRefreshTokenResponse> login(@Valid @RequestBody UserRequest userRequest, HttpServletResponse response) {
        return ResponseEntity.ok(userService.login(userRequest, response));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(userService.logout(request, response));
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        try {
            UserAndTokenRequest userAndTokenRequest = refreshTokenService.accessTokenReissue(request, response);
            return ResponseEntity.ok(userAndTokenRequest);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}
