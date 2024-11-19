package org.example.logintojwt.service;

import jakarta.servlet.http.HttpServletResponse;
import org.example.logintojwt.entity.User;
import org.example.logintojwt.entity.request.UserAndTokenRequest;
import org.example.logintojwt.entity.request.UserRequest;
import org.example.logintojwt.entity.response.AccessTokenAndRefreshTokenResponse;
import org.example.logintojwt.entity.response.UserResponse;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Mockito, JUnit 통합 사용
class UserServiceTest {
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
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void testSignup(){
        //Arrange
        String username = "aaaaaa";
        String password = "bbbbbb";
        String encodedPassword = "encodedPassword123";

        UserRequest userRequest = UserRequest.builder()
                .username(username)
                .password(password)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(
                invocation -> {
                    User user = invocation.getArgument(0);
                    idReflection(user, 1L);
                    return user;
                }
        );

        //Act
        UserResponse userResponse = userService.signup(userRequest);

        //Assert
        assertThat(userResponse).isNotNull();
        assertThat(userResponse.getUsername()).isEqualTo(username);
        assertThat(userResponse.getMessage()).isEqualTo("회원 가입 성공");

        // save 호출 확인
        verify(userRepository, times(1)).save(any(User.class));

    }

    @Test
    void testSignupUserAlreadyExists() {
        String username = "aaaaaa";
        String password = "bbbbbb";

        UserRequest userRequest = UserRequest.builder()
                .username(username)
                .password(password)
                .build();

        User existingUser = User.builder()
                .username(username)
                .password("encodedPassword")
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.signup(userRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("이미 존재하는 아이디입니다.");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testPasswordEncryption() {
        String username = "aaaaaa";
        String password = "bbbbbb";
        String encodedPassword = "encodedbbbbbb";

        UserRequest userRequest = UserRequest.builder()
                .username(username)
                .password(password)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocationOnMock -> {
            User user = invocationOnMock.getArgument(0);
            idReflection(user, 1L);
            return user;
        });

        UserResponse userResponse = userService.signup(userRequest);

        assertThat(userResponse).isNotNull();
        assertThat(userResponse.getUsername()).isEqualTo(username);
        assertThat(userResponse.getMessage()).isEqualTo("회원 가입 성공");

        verify(userRepository).save(argThat(user ->
                user.getUsername().equals(username) && user.getPassword().equals(encodedPassword)
        ));
    }

    @Test
    void testLoginSuccess(){
        //준비
        String username = "aaaaaa";
        String password = "bbbbbb";
        String encodedPassword = "encodedbbbbbb";
        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        UserRequest userRequest = UserRequest.builder()
                .username(username)
                .password(password)
                .build();
        //로그인 하면서 authentication생성한것
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(jwtProvider.createAccessToken(authentication)).thenReturn(accessToken);
        when(jwtProvider.createRefreshToken(authentication)).thenReturn(refreshToken);

        //refreshTokenService 설정
        doNothing().when(refreshTokenService).saveRefreshToken(anyString(), anyString(), anyLong());

        //HttpServletResponse 생성
        HttpServletResponse response = spy(HttpServletResponse.class);


        //실행
        AccessTokenAndRefreshTokenResponse result = userService.login(userRequest, response);

        //검증
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo(accessToken);
        assertThat(result.getRefreshToken()).isEqualTo(refreshToken);

        // 쿠키 검증
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(response, times(1)).addHeader(eq(HttpHeaders.SET_COOKIE), captor.capture());

        List<String> cookies = captor.getAllValues();
        assertThat(cookies).hasSize(2);
        assertThat(cookies.get(0)).contains("accessToken=" + accessToken);
        assertThat(cookies.get(1)).contains("refreshToken=" + refreshToken);

        verify(refreshTokenService, times(1)).saveRefreshToken(eq(username), eq(refreshToken), anyLong());


    }

    @Test
    void testLoginFailure() {
        String username = "testuser";
        String password = "wrongpassword";

        UserRequest userRequest = UserRequest.builder()
                .username(username)
                .password(password)
                .build();

        // 인증 실패
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다."));

        HttpServletResponse response = mock(HttpServletResponse.class);

        assertThatThrownBy(() -> userService.login(userRequest, response))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("아이디 또는 비밀번호가 올바르지 않습니다.");

        verify(jwtProvider, never()).createAccessToken(any());
        verify(jwtProvider, never()).createRefreshToken(any());
        verify(response, never()).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
        verify(refreshTokenService, never()).saveRefreshToken(anyString(), anyString(), anyLong());
    }

    private void idReflection(User user, Long id) {
        try {
            Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}

/*  @ExtendWith(MockitoExtension.class)을 사용하지않으면 수동으로 Mockito를 초기화 해야하는 코드
@BeforeEach
void setUp(){
    MockitoAnnotations.openMocks(this);
}*/
