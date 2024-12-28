package org.example.logintojwt;

import lombok.extern.slf4j.Slf4j;
import org.example.logintojwt.config.security.ConfigProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@Configuration
@Slf4j
public class TestService {
    @Autowired
    private ConfigProperties configProperties;


    @Test
    void configTest(){
        long port = configProperties.getPort();
        assertEquals(8443, port, "서버 포트가 올바르게 매핑되지 않았습니다.");
        log.info("Server port: {}", port);


        String keyAlias = configProperties.getSslKeyAlias();
        assertEquals("myapp", keyAlias, "SSL Key Alias가 올바르게 매핑되지 않았습니다.");
        log.info("Server SSL Key Alias: {}", keyAlias);
    }
}
