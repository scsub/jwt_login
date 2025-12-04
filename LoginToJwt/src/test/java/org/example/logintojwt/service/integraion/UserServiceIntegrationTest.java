package org.example.logintojwt.service.integraion;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.example.logintojwt.config.security.CustomUserDetails;
import org.example.logintojwt.entity.User;
import org.example.logintojwt.exception.GlobalExceptionHandler;
import org.example.logintojwt.config.security.JwtProvider;
import org.example.logintojwt.properties.JwtTokenProperties;
import org.example.logintojwt.repository.CartRepository;
import org.example.logintojwt.repository.UserRepository;
import org.example.logintojwt.request.UserLoginRequest;
import org.example.logintojwt.request.UserProfileRequest;
import org.example.logintojwt.request.UserRegistrationRequest;
import org.example.logintojwt.response.LoginSuccessResponse;
import org.example.logintojwt.response.UserResponse;
import org.example.logintojwt.service.RefreshTokenService;
import org.example.logintojwt.service.UserService;
import org.example.logintojwt.testcontainersTest.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(IntegrationTestBase.class)
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
    @Autowired
    private CartRepository cartRepository;

    private UserRegistrationRequest signupRequest;
    private UserLoginRequest correctLoginRequest;
    private UserLoginRequest wrongLoginRequest;
    private UserRegistrationRequest wrongPasswordSignupRequest;

    @BeforeEach
    void init(){
        cleanUp();
        setUp();
    }
    private void cleanUp() {
        userRepository.deleteAll();
    }

    private void setUp() {
        signupRequest = UserRegistrationRequest.builder()
                .username("kimone")
                .password("password1")
                .passwordCheck("password1")
                .phoneNumber("01012345678")
                .email("kimone@gmail.com")
                .address("서울")
                .build();
        wrongPasswordSignupRequest = UserRegistrationRequest.builder()
                .username("kimone")
                .password("password1")
                .passwordCheck("wrongpassword1")
                .phoneNumber("01012345678")
                .email("kimone@gmail.com")
                .address("서울")
                .build();
        correctLoginRequest = UserLoginRequest.builder()
                .username("kimone")
                .password("password1")
                .build();
        wrongLoginRequest = UserLoginRequest.builder()
                .username("parkone")
                .password("password2")
                .build();
    }

    @Test
    @DisplayName("회원 가입 성공 DB에 저장됐는지 확인")
    void signupSuccess() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.username").value("kimone"))
                .andExpect(jsonPath("$.userId").isNumber())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        UserResponse userResponse = objectMapper.readValue(body, UserResponse.class);

        String location = result.getResponse().getHeader(HttpHeaders.LOCATION);
        assertThat(location).isNotEmpty();
        assertThat(location).endsWith("/" + userResponse.getUserId());


        User user = userRepository.findByUsername("kimone").orElseThrow();
        assertThat(user.getId()).isEqualTo(userResponse.getUserId());
        assertThat(passwordEncoder.matches("password1", user.getPassword())).isTrue();

        boolean present = cartRepository.findByUserId(user.getId()).isPresent();
        assertThat(present).isTrue();
    }

    @Test
    @DisplayName("회원가입 실패 존재하는 아이디라 예외 던짐")
    void signupFail() throws Exception {
        // 첫가입
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated());
        // 같은 username으로 추가
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors.username").value("이미 존재하는 아이디입니다"));
    }

    @Test
    @DisplayName("회원가입 실패 비밀번호 다름")
    void signup_wrong_password() throws Exception {
        signupRequest.setPassword("wrongpassword");
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.passwordCheck").value("비밀번호가 일치하지않습니다"))
                .andReturn();
    }

    @Test
    @DisplayName("로그인 성공")
    void loginSuccess() throws Exception {
        userService.signup(signupRequest);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correctLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").isNumber())
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andReturn();

//                .andDo(result -> {
//                    assertThat(result.getResponse().getHeaders(HttpHeaders.SET_COOKIE).size()).isEqualTo(2);
//                });

        List<String> headers = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        assertThat(headers).isNotNull();
        assertThat(headers.size()).isEqualTo(2);
        assertThat(headers.get(0)).contains("accessToken");
        assertThat(headers.get(1)).contains("refreshToken");

        assertThat(headers.get(0)).contains("HttpOnly");
        assertThat(headers.get(1)).contains("HttpOnly");

        String refreshToken = refreshTokenService.getRefreshTokenByUsername(correctLoginRequest.getUsername());
        assertThat(refreshToken).isNotNull();
        //Expected :"refreshToken=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhYmNkZWYiLCJyb2xlcyI6WyJST0xFX1VTRVIiXSwiaWF0IjoxNzM1OTc3NTMxLCJleHAiOjE3MzY1ODIzMzF9.lvE06d7AxTtfIqWZLoFEawW14dudXfGDqzuZJ_ZoOMo; Path=/; Max-Age=604800; Expires=Sat, 11 Jan 2025 07:58:51 GMT; Secure; Ht ...
        // Actual : 은 refreshToken=과 토큰뒤에 ; Path=/; Max-이런것들이 없는 형태이니 잘라내서 검사
        assertThat(refreshToken).isEqualTo(headers.get(1).split(";")[0].split("=")[1]);

    }

    @Test
    @DisplayName("로그인 실패 비밀번호 잘못 입력 401 예외 던짐")
    void loginFailWrongPassword() throws Exception {
        userService.signup(signupRequest);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongLoginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("사용자 정보 수정")
    void changeProfileSuccess() throws Exception {
        userService.signup(signupRequest);
        User user = userRepository.findByUsername(signupRequest.getUsername()).orElseThrow();
        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(customUserDetails, "", customUserDetails.getAuthorities());

        UserProfileRequest userProfileRequest = UserProfileRequest.builder()
                .password("changedPassword")
                .email("changed@gmail.com")
                .address("changedAddress")
                .phoneNumber("01088888888")
                .build();

        mockMvc.perform(patch("/api/users/me")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authenticationToken)) // 인증된 사용자 넣기
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userProfileRequest)))
                .andExpect(status().isOk());
        User savedUser = userRepository.findByUsername("kimone").orElseThrow();
        assertThat(savedUser.getEmail()).isEqualTo("changed@gmail.com");
        assertThat(savedUser.getAddress()).isEqualTo("changedAddress");
        assertThat(savedUser.getPhoneNumber()).isEqualTo("01088888888");


    }

    @Test
    @DisplayName("사용자 정보 수정 실패 인증된 사용자 정보 없음")
    void changeProfileNoAuth() throws Exception {
        UserProfileRequest userProfileRequest = UserProfileRequest.builder()
                .password("changedPassword")
                .email("changed@gmail.com")
                .address("changedAddress")
                .phoneNumber("01088888888")
                .build();

        mockMvc.perform(patch("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userProfileRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void deleteUserSuccess() throws Exception {
        // 회원가입과 로그인
        MockHttpServletResponse response = new MockHttpServletResponse();
        userService.signup(signupRequest);
        userService.login(correctLoginRequest, response);
        // 리프레시 토큰
        String refreshToken = refreshTokenService.getRefreshTokenByUsername(correctLoginRequest.getUsername());
        assertThat(refreshToken).isNotNull();
        // 인증용
        User user = userRepository.findByUsername(correctLoginRequest.getUsername()).orElseThrow();
        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(customUserDetails, "", customUserDetails.getAuthorities());

        MvcResult result = mockMvc.perform(delete("/api/users/me")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authenticationToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("삭제 완료"))
                .andReturn();

        assertThat(userRepository.findByUsername(correctLoginRequest.getUsername())).isEmpty();
        String refreshTokenByUsername = refreshTokenService.getRefreshTokenByUsername(correctLoginRequest.getUsername());
        assertThat(refreshTokenByUsername).isNull();


    }

    @Test
    @DisplayName("로그아웃 테스트")
    void logoutSuccess() throws Exception {
        // 우선 로그인을 해서 mockHttpServletResponse에 토큰을 발급받고
        MockHttpServletResponse response = new MockHttpServletResponse();
        userService.signup(signupRequest);
        userService.login(correctLoginRequest, response);
        // 로그아웃 하려면 우선 인증된 유저가 있어야하니 인증된 유저를 만들어주고
        User user = userRepository.findByUsername(correctLoginRequest.getUsername()).orElseThrow();
        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(customUserDetails, "", customUserDetails.getAuthorities());
        // 로그아웃할때 쿠키에서 토큰을 얻어와 토큰으로 유저의 리프레시 토큰을 삭제하여 로그인 상태를 해제하기 위해 위선 리프레시 토큰을 얻고
        List<String> cookies = response.getHeaders(HttpHeaders.SET_COOKIE);
        assertThat(cookies).hasSize(2);
        String refreshToken = cookies.get(1).split(";")[0].split("=")[1];
        assertThat(refreshToken).isEqualTo(refreshTokenService.getRefreshTokenByUsername(correctLoginRequest.getUsername()));
        // 로그아웃 테스트할떄 리프레시 토큰과 인증된 유저를 넣어서 테스트한다
        MvcResult result = mockMvc.perform(post("/api/auth/logout")
                        .cookie(new Cookie("refreshToken", refreshToken))
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authenticationToken)))
                .andExpect(status().isOk())
                .andReturn();
        // 검증
        List<String> headers = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        assertThat(headers).hasSize(2);
        assertThat(headers.get(0)).contains("Max-Age=0");
        assertThat(headers.get(1)).contains("Max-Age=0");

        String refreshTokenByUsername = refreshTokenService.getRefreshTokenByUsername(correctLoginRequest.getUsername());
        assertThat(refreshTokenByUsername).isNull();
    }
}
