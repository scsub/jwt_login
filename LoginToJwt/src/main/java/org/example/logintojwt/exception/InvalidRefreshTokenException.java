package org.example.logintojwt.exception;

import lombok.Getter;

@Getter
public class InvalidRefreshTokenException extends RuntimeException {
    private final String field;
    public InvalidRefreshTokenException(String field,String message) {
        super(message);
        this.field = field;
    }
}
