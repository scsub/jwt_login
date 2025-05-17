package org.example.logintojwt.exception;

import lombok.Getter;

@Getter
public class DuplicatedProductException extends RuntimeException {
    private final String field;
    public DuplicatedProductException(String field, String message) {
        super(message);
        this.field = field;
    }
}
