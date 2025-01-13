package org.example.logintojwt.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.logintojwt.config.security.CustomUserDetailsService;
import org.example.logintojwt.entity.Role;
import org.example.logintojwt.entity.User;
import org.example.logintojwt.properties.JwtTokenProperties;
import org.example.logintojwt.request.RefreshTokenRequest;
import org.example.logintojwt.request.UserLoginRequest;
import org.example.logintojwt.request.UserProfileRequest;
import org.example.logintojwt.request.UserRegistrationRequest;
import org.example.logintojwt.response.AccessTokenAndRefreshTokenResponse;
import org.example.logintojwt.response.LoginSuccessResponse;
import org.example.logintojwt.response.UserResponse;
import org.example.logintojwt.exception.UserAlreadyExistsException;
import org.example.logintojwt.jwt.JwtProvider;
import org.example.logintojwt.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Mockito, JUnit 통합 사용
class UserServiceUnitTest {
    @InjectMocks // 테스트할 클래스의 인스턴스 생성
    private UserService userService;

    @Mock // 의존성 주입할 객체 생성
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private JwtTokenProperties jwtTokenProperties;
    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RefreshTokenRequest refreshTokenRequest;

    @Test
    void  successLogout(){
        String refreshToken = "refreshToken";
        String username = "username";
        when(refreshTokenService.getRefreshToken(request)).thenReturn(refreshToken);
        when(jwtProvider.getUsername(refreshToken)).thenReturn(username);

        String logoutText = userService.logout(request, response);

        assertThat("로그아웃 됨").isEqualTo(logoutText);

        verify(refreshTokenService,times(1)).deleteRefreshToken(username);
        verify(refreshTokenService, times(1)).getRefreshToken(request);
        verify(jwtProvider, times(1)).getUsername(refreshToken);

        ArgumentCaptor<String> headerNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> headerValueCaptor = ArgumentCaptor.forClass(String.class);

        verify(response, times(2)).addHeader(headerNameCaptor.capture(), headerValueCaptor.capture());

        assertThat(headerNameCaptor.getAllValues()).hasSize(2);
        assertThat(headerValueCaptor.getAllValues()).hasSize(2);
    }



    @Test
    void deleteUserAndRefreshToken() {
        User user = createUser();
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("testUsername");

        userService.deleteUser(userDetails);

        verify(refreshTokenService).deleteRefreshToken("testUsername");
        verify(userRepository).deleteByUsername("testUsername");
    }

    @Test
    void successSignup(){
        UserRegistrationRequest userRequest = createUserRegisterRequest();
        String encodedPassword = "encodedPassword";

        when(userRepository.findByUsername(userRequest.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(userRequest.getPassword())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse userResponse = userService.signup(userRequest);

        assertThat(userResponse).isNotNull();
        assertThat(userResponse.getUsername()).isEqualTo(userRequest.getUsername());
        assertThat(userResponse.getMessage()).isEqualTo("회원 가입 성공");

        // save 호출 확인
        verify(userRepository, times(1)).save(any(User.class));

    }

    @Test
    void signupUserAlreadyExists() {
        UserRegistrationRequest userRequest = createUserRegisterRequest();
        User user = createUser();

        when(userRepository.findByUsername(userRequest.getUsername())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.signup(userRequest)).isInstanceOf(UserAlreadyExistsException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void passwordEncryption() {
        UserRegistrationRequest userRequest = createUserRegisterRequest();
        String encodedPassword = "encodedPassword";

        when(userRepository.findByUsername(userRequest.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(userRequest.getPassword())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));


        UserResponse userResponse = userService.signup(userRequest);

        assertThat(userResponse).isNotNull();
        assertThat(userResponse.getUsername()).isEqualTo(userRequest.getUsername());
        assertThat(userResponse.getMessage()).isEqualTo("회원 가입 성공");

        verify(userRepository).save(argThat(user ->
                user.getUsername().equals(userRequest.getUsername()) &&
                user.getPassword().equals(encodedPassword)
        ));
    }

    @Test
    void loginSuccess(){
        UserLoginRequest userLoginRequest = createUserLoginRequest();
        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtProvider.createAccessToken(authentication)).thenReturn(accessToken);
        when(jwtProvider.createRefreshToken(authentication)).thenReturn(refreshToken);

        //refreshTokenService 설정 로그인때는 굳이 필요없음

        doNothing().when(refreshTokenService).saveRefreshToken(any(RefreshTokenRequest.class));

        when(jwtTokenProperties.getRefreshValidTime()).thenReturn(604800L);
        when(jwtTokenProperties.getAccessValidTime()).thenReturn(3600L);

        //HttpServletResponse 생성
        HttpServletResponse response = mock(HttpServletResponse.class);


        //실행
        LoginSuccessResponse result = userService.login(userLoginRequest, response);


        //검증
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("로그인 성공");

        // 쿠키 검증
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(response, times(2)).addHeader(eq(HttpHeaders.SET_COOKIE), captor.capture());

        List<String> cookies = captor.getAllValues();
        assertThat(cookies).hasSize(2);
        assertThat(cookies.get(0)).contains("accessToken=" + accessToken);
        assertThat(cookies.get(1)).contains("refreshToken=" + refreshToken);

        verify(refreshTokenService, times(1)).saveRefreshToken(any(RefreshTokenRequest.class));


    }

    @Test
    void LoginFailure() {
        UserLoginRequest userLoginRequest = createUserLoginRequest();

        // 인증 실패
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다."));

        HttpServletResponse response = mock(HttpServletResponse.class);

        assertThatThrownBy(() -> userService.login(userLoginRequest, response))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("아이디 또는 비밀번호가 올바르지 않습니다.");

        verify(jwtProvider, never()).createAccessToken(any());
        verify(jwtProvider, never()).createRefreshToken(any());
        verify(response, never()).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
        verify(refreshTokenService, never()).saveRefreshToken(refreshTokenRequest);
    }

    @Test
    void changeProfileTest(){

        String username = "testUsername";
        UserDetails userDetails = mock(UserDetails.class);
        UserProfileRequest userProfileRequest = UserProfileRequest.builder()
                .password("newPassword")
                .email("newnew@gmail.com")
                .phoneNumber("01087654321")
                .address("대전")
                .build();


        User user = User.builder()
                .username("testUsername")
                .password("testPassword")
                .email("test@example.com")
                .phoneNumber("01012345678")
                .address("서울")
                .roles(List.of(Role.ROLE_USER))
                .build();

        when(userDetails.getUsername()).thenReturn(username);
        when(userRepository.findByUsername("testUsername")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedPassword");

        userService.changeProfile(userDetails, userProfileRequest);


        assertEquals("encodedPassword", user.getPassword());
        assertEquals("newnew@gmail.com", user.getEmail());
        assertEquals("01087654321", user.getPhoneNumber());
        assertEquals("대전", user.getAddress());

        verify(userRepository, times(1)).save(user);
    }

    private User createUser() {
        String username = "originUsername";
        String password = "originPassword";
        String encodedPassword = "encodedPassword";
        String address = " originAddress";
        String phoneNumber = "01012345678";
        String email = "origin@gmail.com";

        return User.builder()
                .username(username)
                .password(encodedPassword)
                .address(address)
                .phoneNumber(phoneNumber)
                .email(email)
                .build();
    }
    private UserRegistrationRequest createUserRegisterRequest() {
        String username = "requestUsername";
        String password = "requestPassword";
        String encodedPassword = "encodedPassword";
        String address = " requestAddress";
        String phoneNumber = "01087654321";
        String email = "request@gmail.com";

        return UserRegistrationRequest.builder()
                .username(username)
                .password(password)
                .address(address)
                .phoneNumber(phoneNumber)
                .email(email)
                .build();

    }
    private UserLoginRequest createUserLoginRequest() {
        String username = "requestUsername";
        String password = "requestPassword";
        String encodedPassword = "encodedPassword";
        String address = " requestAddress";
        String phoneNumber = "01087654321";
        String email = "request@gmail.com";

        return UserLoginRequest.builder()
                .username(username)
                .password(password)
                .build();

    }


}

