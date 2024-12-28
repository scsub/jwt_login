package org.example.logintojwt.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.logintojwt.request.UserLoginRequest;
import org.example.logintojwt.request.UserProfileRequest;
import org.example.logintojwt.request.UserRegistrationRequest;
import org.example.logintojwt.request.UserAndTokenRequest;
import org.example.logintojwt.response.AccessTokenAndRefreshTokenResponse;
import org.example.logintojwt.response.UserResponse;
import org.example.logintojwt.service.RefreshTokenService;
import org.example.logintojwt.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody UserRegistrationRequest userRegistrationRequest) {
        return ResponseEntity.ok(userService.signup(userRegistrationRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AccessTokenAndRefreshTokenResponse> login(@Valid @RequestBody UserLoginRequest userLoginRequest, HttpServletResponse response) {
        return ResponseEntity.ok(userService.login(userLoginRequest, response));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(userService.logout(request, response));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> editUserProfile(@RequestBody @Valid UserProfileRequest userProfileRequest, @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        log.info("로그 ProfileController username : {}", username);
        userService.changeProfile(username, userProfileRequest);
        return ResponseEntity.ok().build();
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
