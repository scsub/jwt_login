package org.example.logintojwt.exception;

import lombok.Getter;

@Getter
public class ProductNotFoundException extends RuntimeException {
    private final String field;
    public ProductNotFoundException(String field,String message) {
        super(message);
        this.field = field;
    }
}
