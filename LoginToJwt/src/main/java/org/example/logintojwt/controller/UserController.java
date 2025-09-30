package org.example.logintojwt.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.logintojwt.config.security.CustomUserDetails;
import org.example.logintojwt.request.UserPasswordChangeRequest;
import org.example.logintojwt.request.UserProfileUpdateRequest;
import org.example.logintojwt.request.UserRegistrationRequest;
import org.example.logintojwt.response.SuccessResponse;
import org.example.logintojwt.response.UserProfileResponse;
import org.example.logintojwt.response.UserResponse;
import org.example.logintojwt.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal CustomUserDetails userDetails, HttpServletResponse response) {
        Long id = userDetails.getId();
        String username = userDetails.getUsername();
        userService.deleteUser(id,username,response);
        SuccessResponse successResponse = new SuccessResponse("삭제 완료");
        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }

    @PatchMapping("/me")
    public ResponseEntity<SuccessResponse> editUserProfileWithoutPassword(@RequestBody @Valid UserProfileUpdateRequest userProfileUpdateRequest, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        userService.changeProfile(userId, userProfileUpdateRequest);
        SuccessResponse successResponse = new SuccessResponse("수정 완료");
        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }

    @PatchMapping("/me/password")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody UserPasswordChangeRequest userPasswordChangeRequest) {
        Long userId = userDetails.getId();
        String originalPassword = userPasswordChangeRequest.getOriginalPassword();
        String newPassword = userPasswordChangeRequest.getNewPassword();
        String confirmNewPassword = userPasswordChangeRequest.getConfirmNewPassword();
        log.warn(String.valueOf(userId));
        log.warn(originalPassword);
        log.warn(newPassword);
        log.warn(confirmNewPassword);
        userService.changePassword(userId, originalPassword, newPassword, confirmNewPassword);
        return ResponseEntity.ok().build();

    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long id = userDetails.getId();
        UserProfileResponse userProfile = userService.getUserProfile(id);
        return ResponseEntity.ok().body(userProfile);
    }
}
