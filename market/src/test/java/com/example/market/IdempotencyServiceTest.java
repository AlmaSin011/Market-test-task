package com.example.market;

import com.example.market.dto.response.OrderResponse;
import com.example.market.service.IdempotencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

public class IdempotencyServiceTest {

    private IdempotencyService idempotencyService;

    @BeforeEach
    void setUp() {
        idempotencyService = new IdempotencyService();
    }

    @Test
    void testSaveAndContainsAndGet() {
        String key = "test-key";
        OrderResponse response = OrderResponse.builder()
                .orderId(UUID.randomUUID())
                .accountId("acc1")
                .symbol("BTC")
                .side("BUY")
                .quantity(10)
                .executedPrice(BigDecimal.valueOf(30000))
                .executedAt(LocalDateTime.now())
                .build();

        assertThat(idempotencyService.contains(key)).isFalse();

        idempotencyService.save(key, response);

        assertThat(idempotencyService.contains(key)).isTrue();
        assertThat(idempotencyService.get(key)).isEqualTo(response);
    }

    @Test
    void testGetWhenNotPresent() {
        String missingKey = "missing-key";
        assertThat(idempotencyService.get(missingKey)).isNull();
    }

    @Test
    void testOverwriteExistingKey() {
        String key = "overwrite-key";

        OrderResponse first = OrderResponse.builder()
                .orderId(UUID.randomUUID())
                .accountId("acc1")
                .symbol("ETH")
                .side("SELL")
                .quantity(1)
                .executedPrice(BigDecimal.valueOf(2000))
                .executedAt(LocalDateTime.now())
                .build();

        OrderResponse second = OrderResponse.builder()
                .orderId(UUID.randomUUID())
                .accountId("acc1")
                .symbol("ETH")
                .side("SELL")
                .quantity(1)
                .executedPrice(BigDecimal.valueOf(2100))
                .executedAt(LocalDateTime.now())
                .build();

        idempotencyService.save(key, first);
        assertThat(idempotencyService.get(key)).isEqualTo(first);

        idempotencyService.save(key, second);
        assertThat(idempotencyService.get(key)).isEqualTo(second);
    }
}
