package org.example.logintojwt.service.unit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.assertj.core.api.Assertions;
import org.example.logintojwt.config.security.CustomUserDetailsService;
import org.example.logintojwt.entity.Cart;
import org.example.logintojwt.entity.Role;
import org.example.logintojwt.entity.User;
import org.example.logintojwt.exception.NotSamePasswordException;
import org.example.logintojwt.exception.UserNotFoundException;
import org.example.logintojwt.properties.JwtTokenProperties;
import org.example.logintojwt.repository.CartRepository;
import org.example.logintojwt.repository.RefreshTokenRepository;
import org.example.logintojwt.request.*;
import org.example.logintojwt.response.LoginSuccessResponse;
import org.example.logintojwt.response.UserProfileResponse;
import org.example.logintojwt.response.UserResponse;
import org.example.logintojwt.exception.UserAlreadyExistsException;
import org.example.logintojwt.config.security.JwtProvider;
import org.example.logintojwt.repository.UserRepository;
import org.example.logintojwt.service.RefreshTokenService;
import org.example.logintojwt.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private RefreshTokenRequest refreshTokenRequest;
    @Mock
    private Authentication authentication;
    @Mock
    private CartRepository cartRepository;
    @Captor
    private ArgumentCaptor<String> headersCaptor;

    @Test
    @DisplayName("로그아웃 테스트")
    void successLogout() {
        String refreshToken = "refreshToken";
        String username = "username";
        when(refreshTokenService.getRefreshToken(request)).thenReturn(refreshToken);
        when(jwtProvider.getUsername(refreshToken)).thenReturn(username);
        // 실행
        String logoutText = userService.logout(request, response);

        assertThat("로그아웃 됨").isEqualTo(logoutText);

        verify(refreshTokenService, times(1)).getRefreshToken(request);
        verify(jwtProvider, times(1)).getUsername(refreshToken);
        verify(refreshTokenService, times(1)).deleteRefreshToken(username);

        verify(response, times(2)).addHeader(eq(HttpHeaders.SET_COOKIE), headersCaptor.capture());
        List<String> cookies = headersCaptor.getAllValues();
        assertThat(cookies).hasSize(2);
    }

    @Test
    @DisplayName("유저삭제하면서 리프레시토큰도 삭제")
    void deleteUserAndRefreshToken() {
        User savedUser = User.builder()
                .id(1L)
                .username("requestUsername")
                .password("encodedPassword")
                .address("requestAddress")
                .phoneNumber("01087654321")
                .email("request@gmail.com")
                .build();
        // 실행
        userService.deleteUser(savedUser.getId(), savedUser.getUsername(), response);
        // 검증
        verify(userRepository).deleteById(savedUser.getId());
        verify(refreshTokenService).deleteRefreshToken(savedUser.getUsername());

        verify(response, times(2)).addHeader(eq(HttpHeaders.SET_COOKIE), headersCaptor.capture());
        List<String> allValues = headersCaptor.getAllValues();
        assertThat(allValues).hasSize(2);

    }

    @Test
    @DisplayName("회원가입 성공")
    void successSignup() {
        UserRegistrationRequest userRegistrationRequest = UserRegistrationRequest.builder()
                .username("requestUsername")
                .password("requestPassword")
                .passwordCheck("requestPassword")
                .address("requestAddress")
                .phoneNumber("01087654321")
                .email("request@gmail.com")
                .build();

        User savedUser = User.builder()
                .id(1L)
                .username("requestUsername")
                .password("encodedPassword")
                .address("requestAddress")
                .phoneNumber("01087654321")
                .email("request@gmail.com")
                .build();

        when(userRepository.findByUsername(userRegistrationRequest.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(userRegistrationRequest.getPassword())).thenReturn(savedUser.getPassword());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse userResponse = userService.signup(userRegistrationRequest);

        // response 확인
        assertThat(userResponse).isNotNull();
        assertThat(userResponse.getUsername()).isEqualTo(savedUser.getUsername());
        assertThat(userResponse.getUserId()).isEqualTo(savedUser.getId());

        // 호출 확인
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode(userRegistrationRequest.getPassword());
        verify(cartRepository, times(1)).save(any(Cart.class));

    }

    @Test
    @DisplayName("중복된 회원가입")
    void signupUserAlreadyExists() {
        UserRegistrationRequest userRegistrationRequest = UserRegistrationRequest.builder()
                .username("requestUsername")
                .password("requestPassword")
                .passwordCheck("requestPassword")
                .address("requestAddress")
                .phoneNumber("01087654321")
                .email("request@gmail.com")
                .build();
        User savedUser = User.builder()
                .id(1L)
                .username("requestUsername")
                .password("encodedPassword")
                .address("requestAddress")
                .phoneNumber("01087654321")
                .email("request@gmail.com")
                .build();

        when(userRepository.findByUsername(userRegistrationRequest.getUsername())).thenReturn(Optional.of(savedUser));
        assertThatThrownBy(() -> userService.signup(userRegistrationRequest)).isInstanceOf(UserAlreadyExistsException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("비밀번호 확인 틀림")
    void signup_Wrong_Password() {
        UserRegistrationRequest userRegistrationRequest = UserRegistrationRequest.builder()
                .username("requestUsername")
                .password("requestPassword")
                .passwordCheck("wrongPassword")
                .address("requestAddress")
                .phoneNumber("01087654321")
                .email("request@gmail.com")
                .build();

        assertThatThrownBy(() -> userService.signup(userRegistrationRequest)).isInstanceOf(NotSamePasswordException.class);
        verify(userRepository, never()).save(any(User.class));

    }

    @Test
    @DisplayName("로그인 성공")
    void loginSuccess() {
        UserLoginRequest userLoginRequest = UserLoginRequest.builder()
                .username("requestUsername")
                .password("requestPassword")
                .build();
        User user = User.builder()
                .id(1L)
                .username("requestUsername")
                .password("encodedPassword")
                .roles(List.of(Role.ROLE_USER))
                .build();

        String accessToken = "access-token";
        String refreshToken = "refresh-token";


        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication); // UsernamePasswordAuthenticationToken 생성 확인
        when(jwtProvider.createAccessToken(authentication)).thenReturn(accessToken); // 엑세스 토큰 생성
        when(jwtProvider.createRefreshToken(authentication)).thenReturn(refreshToken); // 리프레시 토큰 생성
        when(jwtTokenProperties.getRefreshValidTime()).thenReturn(604800L);
        when(jwtTokenProperties.getAccessValidTime()).thenReturn(3600L);
        when(userRepository.findByUsername(userLoginRequest.getUsername())).thenReturn(Optional.of(user));
        //실행
        LoginSuccessResponse result = userService.login(userLoginRequest, response);
        // 결과 값 검증
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(user.getId());
        assertThat(result.getRoles()).isEqualTo(user.getRoles());
        // 인증 검증
        ArgumentCaptor<UsernamePasswordAuthenticationToken> authenticationCaptor = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(authenticationCaptor.capture());
        UsernamePasswordAuthenticationToken authenticationCaptorValue = authenticationCaptor.getValue();
        assertThat(authenticationCaptorValue.getPrincipal()).isEqualTo(userLoginRequest.getUsername());
        assertThat(authenticationCaptorValue.getCredentials()).isEqualTo(userLoginRequest.getPassword());
        // jwt토큰 생성 검증
        verify(jwtProvider).createAccessToken(authentication);
        verify(jwtProvider).createRefreshToken(authentication);
        // 리프레시 토큰 저장 검증
        ArgumentCaptor<RefreshTokenRequest> refreshTokenRequestCaptor = ArgumentCaptor.forClass(RefreshTokenRequest.class);
        verify(refreshTokenService).saveRefreshToken(refreshTokenRequestCaptor.capture());
        RefreshTokenRequest refreshTokenRequestCaptorValue = refreshTokenRequestCaptor.getValue();
        assertThat(refreshTokenRequestCaptorValue).isNotNull();
        assertThat(refreshTokenRequestCaptorValue.getUsername()).isEqualTo(userLoginRequest.getUsername());
        assertThat(refreshTokenRequestCaptorValue.getToken()).isEqualTo(refreshToken);
        // 쿠키 검증
        verify(response, times(2)).addHeader(eq(HttpHeaders.SET_COOKIE), headersCaptor.capture());

        List<String> cookies = headersCaptor.getAllValues();
        assertThat(cookies).hasSize(2);
        assertThat(cookies.get(0)).contains("accessToken=" + accessToken);
        assertThat(cookies.get(1)).contains("refreshToken=" + refreshToken);
    }

    @Test
    @DisplayName("로그인 실패 비밀번호 틀림")
    void LoginFailure() {
        UserLoginRequest userLoginRequest = UserLoginRequest.builder()
                .username("requestUsername")
                .password("wrongPassword")
                .build();
        // 실행
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다."));
        //
        assertThatThrownBy(() -> userService.login(userLoginRequest, response))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("아이디 또는 비밀번호가 올바르지 않습니다.");

        verify(jwtProvider, never()).createAccessToken(any());
        verify(jwtProvider, never()).createRefreshToken(any());
        verify(response, never()).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
        verify(refreshTokenService, never()).saveRefreshToken(refreshTokenRequest);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("로그인 실패 없는 유저")
    void login_failure_user_not_found() {
        UserLoginRequest userLoginRequest = UserLoginRequest.builder()
                .username("requestUsername")
                .password("requestPassword")
                .build();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(UsernameNotFoundException.class);
        // 실행
        assertThatThrownBy(() -> userService.login(userLoginRequest, response)).isInstanceOf(UsernameNotFoundException.class);
        verify(jwtProvider, never()).createAccessToken(any());
        verify(jwtProvider, never()).createRefreshToken(any());
    }

    @Test
    @DisplayName("유저 프로필 변경 성공 비밀번호 제외")
    void changeProfileTest() {
        UserProfileUpdateRequest userProfileRequest = UserProfileUpdateRequest.builder()
                .email("new@gmail.com")
                .phoneNumber("01011111111")
                .address("newAddress")
                .build();
        User user = User.builder()
                .id(1L)
                .username("requestUsername")
                .password("encodedPassword")
                .address("requestAddress")
                .phoneNumber("01087654321")
                .email("request@gmail.com")
                .roles(List.of(Role.ROLE_USER))
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        // 실행
        userService.changeProfile(user.getId(), userProfileRequest);
        // 검증
        assertThat(user.getEmail()).isEqualTo(userProfileRequest.getEmail());
        assertThat(user.getPhoneNumber()).isEqualTo(userProfileRequest.getPhoneNumber());
        assertThat(user.getAddress()).isEqualTo(userProfileRequest.getAddress());
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo(userProfileRequest.getEmail());
        assertThat(savedUser.getPhoneNumber()).isEqualTo(userProfileRequest.getPhoneNumber());
        assertThat(savedUser.getAddress()).isEqualTo(userProfileRequest.getAddress());
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePasswordTest() {
        Long userId = 1L;
        String encodedNewPassword = "encodedNewPassword";
        UserPasswordChangeRequest changeRequest = UserPasswordChangeRequest.builder()
                .originalPassword("originalPassword")
                .newPassword("newPassword")
                .confirmNewPassword("newPassword")
                .build();
        User user = User.builder()
                .id(1L)
                .username("requestUsername")
                .password("encodedPassword")
                .address("requestAddress")
                .phoneNumber("01087654321")
                .email("request@gmail.com")
                .roles(List.of(Role.ROLE_USER))
                .build();

        // 설정
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(changeRequest.getOriginalPassword(), user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(changeRequest.getNewPassword())).thenReturn(encodedNewPassword);
        // 실행
        userService.changePassword(userId, changeRequest);
        // 비밀번호 바뀐거 검증
        verify(passwordEncoder, times(1)).encode(changeRequest.getNewPassword());
        assertThat(user.getPassword()).isEqualTo(encodedNewPassword);
    }

    @Test
    @DisplayName("기존비밀번호 틀림")
    void change_password_wrong_password() {
        Long userId = 1L;
        String encodedNewPassword = "encodedNewPassword";
        UserPasswordChangeRequest changeRequest = UserPasswordChangeRequest.builder()
                .originalPassword("wrongPassword")
                .newPassword("newPassword")
                .confirmNewPassword("newPassword")
                .build();
        User user = User.builder()
                .id(1L)
                .username("requestUsername")
                .password("encodedPassword")
                .address("requestAddress")
                .phoneNumber("01087654321")
                .email("request@gmail.com")
                .roles(List.of(Role.ROLE_USER))
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(changeRequest.getOriginalPassword(), user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(userId, changeRequest)).isInstanceOf(IllegalStateException.class);
        assertThat(user.getPassword()).isNotEqualTo(encodedNewPassword);
    }

    @Test
    @DisplayName("유저프로필 얻어오기 성공")
    void get_user_profile() {
        User user = User.builder()
                .id(1L)
                .username("requestUsername")
                .password("encodedPassword")
                .address("requestAddress")
                .phoneNumber("01087654321")
                .email("request@gmail.com")
                .roles(List.of(Role.ROLE_USER))
                .build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        //실행
        UserProfileResponse userProfile = userService.getUserProfile(user.getId());
        // 검증
        assertThat(userProfile.getUsername()).isEqualTo(user.getUsername());
    }

    @Test
    @DisplayName("유저프로필 얻어오는데 유저가 없음")
    void get_user_profile_not_found() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUserProfile(1L)).isInstanceOf(UsernameNotFoundException.class);
    }

//    private User savedUser() {
//        String username = "requestUsername";
//        String password = "requestPassword";
//        String encodedPassword = "encodedPassword";
//        String address = " requestAddress";
//        String phoneNumber = "01087654321";
//        String email = "request@gmail.com";
//
//        return User.builder()
//                .id(1L)
//                .username(username)
//                .password(encodedPassword)
//                .address(address)
//                .phoneNumber(phoneNumber)
//                .email(email)
//                .build();
//
//    }


//    private UserRegistrationRequest createUserRegisterRequest() {
//        String username = "requestUsername";
//        String password = "requestPassword";
//        String encodedPassword = "encodedPassword";
//        String address = " requestAddress";
//        String phoneNumber = "01087654321";
//        String email = "request@gmail.com";
//
//
//        return UserRegistrationRequest.builder()
//                .username(username)
//                .password(password)
//                .address(address)
//                .phoneNumber(phoneNumber)
//                .email(email)
//                .build();
//    }
//
//    private UserLoginRequest createUserLoginRequest() {
//        String username = "requestUsername";
//        String password = "requestPassword";
//        String encodedPassword = "encodedPassword";
//        String address = " requestAddress";
//        String phoneNumber = "01087654321";
//        String email = "request@gmail.com";
//
//        return UserLoginRequest.builder()
//                .username(username)
//                .password(password)
//                .build();
//
//    }

}

