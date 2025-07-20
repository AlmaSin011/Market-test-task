package com.example.pricefeed;

import com.example.pricefeed.exception.PriceNotFoundException;
import com.example.pricefeed.service.PriceServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PriceServiceImplTest {

    private final PriceServiceImpl priceService = new PriceServiceImpl();

    @Test
    void getPrice_shouldReturnCorrectPrice_whenSymbolExists() {
        // given
        String symbol = "AAPL";

        // when
        BigDecimal price = priceService.getPrice(symbol);

        // then
        assertNotNull(price);
        assertEquals(new BigDecimal("210.550000"), price);
    }

    @Test
    void getPrice_shouldBeCaseInsensitive() {
        // given
        String symbol = "TSLA";

        // when
        BigDecimal price = priceService.getPrice(symbol);

        // then
        assertEquals(new BigDecimal("123.456789"), price);
    }

    @Test
    void getPrice_shouldThrowException_whenSymbolNotFound() {
        // given
        String symbol = "INVALID";

        // when + then
        PriceNotFoundException exception = assertThrows(PriceNotFoundException.class, () -> {
            priceService.getPrice(symbol);
        });

        assertEquals("Price not found for symbol: INVALID", exception.getMessage());
    }
}
