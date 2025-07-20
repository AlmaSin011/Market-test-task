package com.example.market.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class PriceFeedUnavailableException extends RuntimeException {

    public PriceFeedUnavailableException(String message) {
        super(message);
    }
}
