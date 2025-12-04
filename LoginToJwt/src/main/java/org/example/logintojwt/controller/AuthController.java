package org.example.logintojwt.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.logintojwt.request.UserAndAccessTokenRequest;
import org.example.logintojwt.request.UserLoginRequest;
import org.example.logintojwt.request.UserRegistrationRequest;
import org.example.logintojwt.response.SuccessResponse;
import org.example.logintojwt.response.LoginSuccessResponse;
import org.example.logintojwt.response.UserResponse;
import org.example.logintojwt.service.RefreshTokenService;
import org.example.logintojwt.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody UserRegistrationRequest userRegistrationRequest) {
        UserResponse userResponse = userService.signup(userRegistrationRequest);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(userResponse.getUserId())
                .toUri();

        return ResponseEntity.created(location).body(userResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginSuccessResponse> login(@Valid @RequestBody UserLoginRequest userLoginRequest, HttpServletResponse response) {
        LoginSuccessResponse loginSuccessResponse = userService.login(userLoginRequest, response);
        return ResponseEntity.ok().body(loginSuccessResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String logoutText = userService.logout(request, response);
        SuccessResponse successResponse = new SuccessResponse(logoutText);
        return ResponseEntity.ok().body(successResponse);
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        UserAndAccessTokenRequest userAndAccessTokenRequest = refreshTokenService.accessTokenReissue(request, response);
        return ResponseEntity.status(HttpStatus.OK).body(userAndAccessTokenRequest);
    }
}
