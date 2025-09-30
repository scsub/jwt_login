package org.example.logintojwt.exception;

import io.jsonwebtoken.JwtException;
import org.example.logintojwt.response.ErrorResponse;
import org.example.logintojwt.response.ValidationErrorResponse;
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
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // 필드명 : 메시지 형식으로 통일
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ValidationErrorResponse> handlerUserAlreadyExistsException(UserAlreadyExistsException e) {
        Map<String, String> errors = Map.of(e.getField(), e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ValidationErrorResponse(errors));
    }

    //@Valid에서 검증 실패하면 이 예외처리가 사용된다
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handlerMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> response = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
        return ResponseEntity.badRequest().body(new ValidationErrorResponse(response));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ValidationErrorResponse> handlerUsernameNotFoundException(UsernameNotFoundException e) {
        Map<String, String> response = Map.of("login", e.getMessage());
        return ResponseEntity.badRequest().body(new ValidationErrorResponse(response));
    }

    @ExceptionHandler(DuplicatedProductException.class)
    public ResponseEntity<ValidationErrorResponse> handlerDuplicatedProductException(DuplicatedProductException e) {
        Map<String, String> errors = Map.of(e.getField(), e.getMessage());
        return ResponseEntity.badRequest().body(new ValidationErrorResponse(errors));
    }

    //  <Map<String, List<String>>> 형태가 굳이 필요하지 않은것 같아서 잠가놓음
    // UserLoginRequest 유효성 검증 예외 처리 ,@Valid
/*    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, List<String>>> handlerMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, List<String>> messages = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> {
            String field = error.getField();
            String defaultMessage = error.getDefaultMessage();
            messages.computeIfAbsent(field, key -> new ArrayList<>()).add(defaultMessage);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(messages);
    }*/

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<?> handlerJwtException(JwtException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ValidationErrorResponse> handlerBadCredentialsException(BadCredentialsException e) {
        Map<String, String> response = Map.of("login", "아이디 혹은 비밀번호가 정확하지 않음");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ValidationErrorResponse(response));
    }

/*    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handlerAuthenticationException(AuthenticationException e) {
        ErrorResponse errorResponse = new ErrorResponse("인증 오류 발생");
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }*/

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ValidationErrorResponse> handlerInvalidRefreshTokenException(InvalidRefreshTokenException e) {
        Map<String, String> response = Map.of(e.getField(), e.getMessage());
        return ResponseEntity.badRequest().body(new ValidationErrorResponse(response));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ValidationErrorResponse> handlerUserNotFoundException(UserNotFoundException e) {
        Map<String, String> response = Map.of(e.getField(), e.getMessage());
        return ResponseEntity.badRequest().body(new ValidationErrorResponse(response));
    }

    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<ValidationErrorResponse> handlerReviewNotFoundException(ReviewNotFoundException e) {
        Map<String, String> response = Map.of(e.getField(), e.getMessage());
        return ResponseEntity.badRequest().body(new ValidationErrorResponse(response));
    }

    @ExceptionHandler(CartItemNotFoundException.class)
    public ResponseEntity<ValidationErrorResponse> handlerCartItemNotFoundException(CartItemNotFoundException e) {
        Map<String, String> response = Map.of(e.getField(), e.getMessage());
        return ResponseEntity.badRequest().body(new ValidationErrorResponse(response));
    }

    @ExceptionHandler(OutOfStockException.class)
    public ResponseEntity<ValidationErrorResponse> handlerOutOfStockException(OutOfStockException e) {
        Map<String, String> response = Map.of(e.getField(), e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ValidationErrorResponse(response));
    }

    @ExceptionHandler(NotSamePasswordException.class)
    public ResponseEntity<?> handlerNotSamePasswordException(NotSamePasswordException e) {
        Map<String, String> response = Map.of(e.getField(), e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ValidationErrorResponse(response));
    }
}

