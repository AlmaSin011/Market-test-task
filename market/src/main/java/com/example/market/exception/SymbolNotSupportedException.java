package com.example.market.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class SymbolNotSupportedException extends RuntimeException {

    public SymbolNotSupportedException(String message) {
        super(message);
    }
}
