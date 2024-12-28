package org.example.logintojwt.config.security;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties(prefix = "server")
@Getter
@Setter
public class ConfigProperties {
    private long port;
    private String sslKeyAlias;
}
