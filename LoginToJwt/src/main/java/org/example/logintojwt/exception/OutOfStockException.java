package org.example.logintojwt.exception;

import lombok.Getter;

@Getter
public class OutOfStockException extends RuntimeException {
    private final String field;
    public OutOfStockException(String field,String message) {
        super(message);
        this.field = field;
    }
}
