package org.example.logintojwt.exception;

import lombok.Getter;

@Getter
public class ReviewNotFoundException extends RuntimeException {
    private final String field;
    public ReviewNotFoundException(String field,String message) {
        super(message);
        this.field = field;
    }
}
