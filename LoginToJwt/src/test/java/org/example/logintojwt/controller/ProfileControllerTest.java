package org.example.logintojwt.controller;

import org.example.logintojwt.config.security.CustomUserDetails;
import org.example.logintojwt.request.UserProfileUpdateRequest;
import org.example.logintojwt.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
        // userDetails, userProfileUpdateRequest 생성
        Long userId = 1L;
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getUsername()).thenReturn("testUsername");
        UserProfileUpdateRequest userProfileUpdateRequest = UserProfileUpdateRequest.builder()
                .email("abcd@gmail.com")
                .phoneNumber("01012345678")
                .address("집주소")
                .build();

        doNothing().when(userService).changeProfile(userId, userProfileUpdateRequest);

        ResponseEntity<?> responseEntity = userController.editUserProfileWithoutPassword(userProfileUpdateRequest, userDetails);

        verify(userService, times(1)).changeProfile(userId, userProfileUpdateRequest);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

    }
}