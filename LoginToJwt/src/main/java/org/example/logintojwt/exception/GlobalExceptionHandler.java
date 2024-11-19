package org.example.logintojwt.exception;

import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
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
    public ResponseEntity<String> handlerUserAlreadyExistsException(UserAlreadyExistsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    // UserRequest 유효성 검증 예외 처리
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
    public ResponseEntity<?> handlerBadCredentialsException(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
    // UsernameNotFoundException 불가능

}
