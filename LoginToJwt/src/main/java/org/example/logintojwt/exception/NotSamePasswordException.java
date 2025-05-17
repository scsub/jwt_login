package org.example.logintojwt.exception;

import lombok.Getter;

@Getter
public class NotSamePasswordException extends RuntimeException {
    private final String field;

    public NotSamePasswordException(String field, String message) {
        super(message);
        this.field = field;
    }
}
