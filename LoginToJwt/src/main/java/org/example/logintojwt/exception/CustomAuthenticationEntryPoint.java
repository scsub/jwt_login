package org.example.logintojwt.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
// 스프링 시큐리티에서 사용
// AuthenticationException 혹은 인증되지 않은 사용자가 제한된 리소스에 접근하려할때 호출됨
// 인증 실패 시의 처리 로직을 정의해야함
// 인증되지 않은 요청이 들어오면 클라이언트에게 응답을 반환하여 인증이 필요하다고 알림

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
