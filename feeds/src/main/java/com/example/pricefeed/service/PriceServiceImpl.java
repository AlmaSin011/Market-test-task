package com.example.pricefeed.service;

import com.example.pricefeed.exception.PriceNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class PriceServiceImpl implements PriceService {

    private static final Map<String, BigDecimal> PRICES = Map.of(
            "AAPL", new BigDecimal("210.550000"),
            "GOOG", new BigDecimal("3050.123456"),
            "TSLA", new BigDecimal("123.456789"),
            "AMZN", new BigDecimal("987.654321")
    );

    @Override
    public BigDecimal getPrice(String symbol) {
        String upperSymbol = symbol.toUpperCase();
        BigDecimal price = PRICES.get(upperSymbol);
        if (price == null) {
            throw new PriceNotFoundException("Price not found for symbol: " + upperSymbol);
        }
        return PRICES.get(symbol.toUpperCase());
    }
}
