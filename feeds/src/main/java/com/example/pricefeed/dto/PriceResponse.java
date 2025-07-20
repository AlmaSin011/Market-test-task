package com.example.pricefeed.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class PriceResponse {

    private String symbol;

    private BigDecimal price;

    public PriceResponse(String symbol, BigDecimal price) {
        this.symbol = symbol;
        this.price = price;
    }
}
