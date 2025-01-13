package org.example.logintojwt.exception;

import io.jsonwebtoken.JwtException;
import org.example.logintojwt.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handlerUserAlreadyExistsException(UserAlreadyExistsException e) {
        ErrorResponse errorResponse = new ErrorResponse("이미 존재하는 아이디입니다");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    // UserLoginRequest 유효성 검증 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, List<String>>> handlerMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, List<String>> messages = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> {
            String field = error.getField();
            String defaultMessage = error.getDefaultMessage();
            messages.computeIfAbsent(field, key -> new ArrayList<>()).add(defaultMessage);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(messages);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<?> handlerJwtException(JwtException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handlerBadCredentialsException(BadCredentialsException e) {
        ErrorResponse errorResponse = new ErrorResponse("아이디 혹은 비밀번호가 정확하지 않음");
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

    }
    // UsernameNotFoundException 불가능

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handlerAuthenticationException(AuthenticationException e) {
        ErrorResponse errorResponse = new ErrorResponse("인증 오류 발생");
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handlerInvalidRefreshTokenException(InvalidRefreshTokenException e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
}

