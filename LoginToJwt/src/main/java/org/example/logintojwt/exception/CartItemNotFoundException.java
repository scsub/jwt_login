package org.example.logintojwt.exception;

import lombok.Getter;

@Getter
public class CartItemNotFoundException extends RuntimeException {
    private final String field;
    public CartItemNotFoundException(String field,String message) {
        super(message);
        this.field = field;
    }
}
