package org.example.logintojwt.jwt;

import org.example.logintojwt.config.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "jwt.secret=wff12foi23goi43g23f13f314tr3c31123e212t3tv32y45y2vt324tv3246v236325v62345v32123ttrwe",
        "refresh-valid-time=15",
        "access-valid-time=604800"
})
@ExtendWith(MockitoExtension.class)
class JwtProviderTest {
    @Autowired
    private JwtProvider jwtProvider;

    private Authentication authentication;

    @BeforeEach
    void init(){
        authentication = new TestingAuthenticationToken(
                "abcdef",
                null,
                //그냥 스트링으로 넣어봤는데 타입변경되서 들어감 이유는 내부에서 GrantedAuthority 타입으로 변환시킴
                List.of(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void testCreateToken() {
        String accessToken = jwtProvider.createAccessToken(authentication);

        assertNotNull(accessToken); // 토큰 발급 검사
        assertEquals("abcdef", jwtProvider.getUsername(accessToken)); // 유저 이름 검사
        assertEquals(List.of("ROLE_USER","ROLE_ADMIN"), jwtProvider.getRoles(accessToken)); // 유저 역할 검사
        assertTrue(jwtProvider.validateToken(accessToken)); // 토큰 유효 검사
    }
}