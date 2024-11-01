package org.example.logintojwt.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.logintojwt.entity.UserAndTokenRequest;
import org.example.logintojwt.entity.UserRequest;
import org.example.logintojwt.entity.UserResponse;
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

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody UserRequest userRequest) {
        return ResponseEntity.ok(userService.signup(userRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserRequest userRequest, HttpServletResponse response) {
        log.info("일단 넘어옴");
        log.info(userRequest.getUsername());
        log.info((userRequest.getPassword()));
        return ResponseEntity.ok(userService.login(userRequest, response));
    }

    /*@PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        userService.logout(request, response); //만들어야함
    }*/

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        try {
            UserAndTokenRequest userAndTokenRequest = userService.accessTokenReissue(request, response);
            return ResponseEntity.ok(userAndTokenRequest);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}
