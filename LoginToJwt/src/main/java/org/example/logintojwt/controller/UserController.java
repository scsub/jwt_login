package org.example.logintojwt.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.logintojwt.request.UserProfileRequest;
import org.example.logintojwt.request.UserRegistrationRequest;
import org.example.logintojwt.response.SuccessResponse;
import org.example.logintojwt.response.UserResponse;
import org.example.logintojwt.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody UserRegistrationRequest userRegistrationRequest) {
        UserResponse userResponse = userService.signup(userRegistrationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteUser(userDetails);
        SuccessResponse successResponse = new SuccessResponse("삭제 완료");
        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }

    @PatchMapping("/me")
    public ResponseEntity<SuccessResponse> editUserProfile(@RequestBody @Valid UserProfileRequest userProfileRequest, @AuthenticationPrincipal UserDetails userDetails) {
        userService.changeProfile(userDetails, userProfileRequest);
        SuccessResponse successResponse = new SuccessResponse("수정 완료");
        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }
}
