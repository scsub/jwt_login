package org.example.logintojwt.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        String exception = (String) request.getAttribute("exception");
        if ("expired".equals(exception)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,"엑세스 토큰 만료");
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증이 필요");
        }

    }
}
