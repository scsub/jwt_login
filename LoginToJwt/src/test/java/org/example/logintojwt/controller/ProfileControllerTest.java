package org.example.logintojwt.controller;

import org.example.logintojwt.config.security.CustomUserDetails;
import org.example.logintojwt.request.UserProfileRequest;
import org.example.logintojwt.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {
    @InjectMocks
    UserController userController;

    @Mock
    private UserService userService;

    @Test
    void  editUserProfileTest(){
        // userDetails, userProfileRequest 생성
        UserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getUsername()).thenReturn("testUsername");
        UserProfileRequest userProfileRequest = UserProfileRequest.builder()
                .password("password")
                .email("abcd@gmail.com")
                .phoneNumber("01012345678")
                .address("집주소")
                .build();

        doNothing().when(userService).changeProfile(userDetails, userProfileRequest);

        ResponseEntity<?> responseEntity = userController.editUserProfile(userProfileRequest, userDetails);

        verify(userService, times(1)).changeProfile(userDetails, userProfileRequest);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

    }
}