package org.example.logintojwt.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("jwt.token")
@Data
public class JwtTokenProperties {
    private long refreshValidTime;
    private long accessValidTime;
    private String secret;
}
