package org.example.logintojwt.exception;

import lombok.Getter;

@Getter
public class UserNotFoundException extends RuntimeException {
    private final String field;
    public UserNotFoundException(String field,String message) {
        super(message);
        this.field = field;
    }
}
