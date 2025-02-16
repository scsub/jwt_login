package org.example.logintojwt.service.integraion;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.logintojwt.entity.User;
import org.example.logintojwt.exception.GlobalExceptionHandler;
import org.example.logintojwt.exception.UserAlreadyExistsException;
import org.example.logintojwt.config.security.JwtProvider;
import org.example.logintojwt.properties.JwtTokenProperties;
import org.example.logintojwt.repository.UserRepository;
import org.example.logintojwt.request.UserLoginRequest;
import org.example.logintojwt.request.UserProfileRequest;
import org.example.logintojwt.request.UserRegistrationRequest;
import org.example.logintojwt.response.LoginSuccessResponse;
import org.example.logintojwt.service.RefreshTokenService;
import org.example.logintojwt.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@Rollback
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY) // 인메모리 db로 자동 교체
@ExtendWith(SpringExtension.class)

public class UserServiceIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private JwtTokenProperties jwtTokenProperties;
    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;
    @Autowired
    private ObjectMapper objectMapper; // JSON 직렬화

    private UserRegistrationRequest userRegistrationRequest;
    private UserLoginRequest userLoginRequest;

    @BeforeEach
    void setUp() {
        userRegistrationRequest = UserRegistrationRequest.builder()
                .username("abcdef")
                .password("123456")
                .phoneNumber("01012345678")
                .email("user@gmail.com")
                .address("대전")
                .build();

        userLoginRequest = UserLoginRequest.builder()
                .username("abcdef")
                .password("123456")
                .build();
    }

    @Test
    @DisplayName("회원 가입 성공: DB에 저장됐는지 확인")
    void signupSuccess() throws Exception {
        String json = objectMapper.writeValueAsString(userRegistrationRequest);
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("abcdef"))
                .andExpect(jsonPath("$.message").value("회원 가입 성공"))
                .andDo(result -> assertThat(userRepository.findByUsername("abcdef")).isPresent());

        User user = userRepository.findByUsername("abcdef").orElse(null);
        assertThat(user).isNotNull();
        assertThat(userRegistrationRequest.getUsername()).isEqualTo(user.getUsername());
        assertThat(passwordEncoder.matches(userRegistrationRequest.getPassword(), user.getPassword())).isTrue();
        assertThat(userRegistrationRequest.getPhoneNumber()).isEqualTo(user.getPhoneNumber());
        assertThat(userRegistrationRequest.getEmail()).isEqualTo(user.getEmail());
        assertThat(userRegistrationRequest.getAddress()).isEqualTo(user.getAddress());
    }

    @Test
    @DisplayName("존재하는 아이디라 UserAlreadyExistsException 예외 던짐")
    void signupFail() throws Exception {
        String json = objectMapper.writeValueAsString(userRegistrationRequest);
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 존재하는 아이디입니다"));

        assertThatThrownBy(() -> userService.signup(userRegistrationRequest)).isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    @DisplayName("로그인 성공 : 토큰 2개있는지, 토큰이 정확한지 확인")
    void loginSuccess() throws Exception {
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();

        userService.signup(userRegistrationRequest);

        String json = objectMapper.writeValueAsString(userLoginRequest);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그인 성공"))
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andDo(result -> {
                    assertThat(result.getResponse().getHeaders(HttpHeaders.SET_COOKIE).size()).isEqualTo(2);
                });



        LoginSuccessResponse loginResponse = userService.login(userLoginRequest, httpServletResponse);

        assertThat(loginResponse).isNotNull();
        assertThat(loginResponse.getMessage()).isEqualTo("로그인 성공");


        List<String> headers = httpServletResponse.getHeaders(HttpHeaders.SET_COOKIE);
        assertThat(headers).isNotNull();
        assertThat(headers.size()).isEqualTo(2);
        assertThat(headers.get(0)).contains("accessToken");
        assertThat(headers.get(1)).contains("refreshToken");

        String refreshToken = refreshTokenService.getRefreshTokenByUsername(userRegistrationRequest.getUsername());

        assertThat(refreshToken).isNotNull();
        //Expected :"refreshToken=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhYmNkZWYiLCJyb2xlcyI6WyJST0xFX1VTRVIiXSwiaWF0IjoxNzM1OTc3NTMxLCJleHAiOjE3MzY1ODIzMzF9.lvE06d7AxTtfIqWZLoFEawW14dudXfGDqzuZJ_ZoOMo; Path=/; Max-Age=604800; Expires=Sat, 11 Jan 2025 07:58:51 GMT; Secure; Ht ...
        // Actual : 은 refreshToken=과 토큰뒤에 ; Path=/; Max-이런것들이 없는 형태이니 잘라내서 검사
        assertThat(refreshToken).isEqualTo(headers.get(1).split(";")[0].split("=")[1]);

    }

    @Test
    @DisplayName("로그인 실패 : 비밀번호 잘못 입력 BadCredentialsException 예외 던짐")
    void loginFailWrongPassword() throws Exception {
        userService.signup(userRegistrationRequest);
        userLoginRequest.setPassword("wrongPassword");
        String json = objectMapper.writeValueAsString(userLoginRequest);
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();

        //

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("아이디 혹은 비밀번호가 정확하지 않음"));

        UserLoginRequest loginRequest = UserLoginRequest.builder()
                .username(userRegistrationRequest.getUsername())
                .password("wrongPassword")
                .build();

        assertThatThrownBy(() -> userService.login(loginRequest, httpServletResponse))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("사용자 정보 수정")
    //@WithMockUser(username = "abcdef", roles = {"USER"}) / with(SecurityMockMvcRequestPostProcessors.user 두가지 방법이있음
    // 테스트 환경에서 인증된 사용자를 securityContext 에 넣는다 스프링 시큐리티에서 인증받는 그것의 모킹
    void changeProfileSuccess() throws Exception {
        userService.signup(userRegistrationRequest);
        UserProfileRequest userProfileRequest = UserProfileRequest.builder()
                .password("111111")
                .email("changed@gmail.com")
                .address("바뀐주소")
                .phoneNumber("01088888888")
                .build();

        String json = objectMapper.writeValueAsString(userProfileRequest);

        mockMvc.perform(patch("/api/users/me")
                        .with(SecurityMockMvcRequestPostProcessors.user("abcdef").roles("USER")) // 인증된 사용자 넣기
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andDo(result -> {
                    User changedUser = userRepository.findByUsername("abcdef").orElseThrow();
                    assertThat(changedUser).isNotNull();
                    assertThat(passwordEncoder.matches("111111", changedUser.getPassword())).isTrue();
                    assertThat(changedUser.getAddress()).isEqualTo("바뀐주소");
                });
    }

    @Test
    @DisplayName("사용자 정보 수정 실패: 인증된 사용자 정보 없음")
    void changeProfileNoAuth() throws Exception {
        UserProfileRequest userProfileRequest = UserProfileRequest.builder()
                .password("111111")
                .email("changed@gmail.com")
                .address("바뀐주소")
                .phoneNumber("01088888888")
                .build();

        String json = objectMapper.writeValueAsString(userProfileRequest);

        mockMvc.perform(patch("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());


    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void deleteUserSuccess() throws Exception {
        userService.signup(userRegistrationRequest);

        mockMvc.perform(delete("/api/users/me")
                        .with(SecurityMockMvcRequestPostProcessors.user("abcdef").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string("삭제 완료"))
                .andDo(result -> {
                    assertThat(userRepository.findByUsername("abcdef")).isEmpty();
                });
    }
}
