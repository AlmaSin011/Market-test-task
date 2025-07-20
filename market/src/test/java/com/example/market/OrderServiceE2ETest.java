package com.example.market;

import com.example.market.client.PriceFeedClient;
import com.example.market.dto.request.OrderRequest;
import com.example.market.dto.response.OrderResponse;
import com.example.market.dto.response.PriceResponse;
import com.example.market.model.Execution;
import com.example.market.model.Order;
import com.example.market.model.OrderSide;
import com.example.market.model.OrderStatus;
import com.example.market.repository.ExecutionRepository;
import com.example.market.repository.OrderRepository;
import com.example.market.service.OrderService;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class OrderServiceE2ETest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("market")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ExecutionRepository executionRepository;

    @MockBean
    private PriceFeedClient priceFeedClient;

    @Autowired
    private MeterRegistry meterRegistry;

    @BeforeEach
    void clean() {
        executionRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Test
    void testPlaceAndGetOrder_E2E() {
        when(priceFeedClient.getPrice("AAPL"))
                .thenReturn(new PriceResponse("AAPL",BigDecimal.valueOf(123.450000)));

        OrderRequest request = OrderRequest.builder()
                .accountId("acc123")
                .symbol("AAPL")
                .side(OrderSide.BUY)
                .quantity(10)
                .build();

        OrderResponse response = orderService.placeOrder(request);

        assertThat(response.getAccountId()).isEqualTo("acc123");
        assertThat(response.getSymbol()).isEqualTo("AAPL");
        assertThat(response.getExecutedPrice()).isEqualTo(BigDecimal.valueOf(123.450000));
        assertThat(response.getOrderId()).isNotNull();

        Optional<Order> dbOrder = orderRepository.findById(response.getOrderId());
        assertThat(dbOrder).isPresent();
        assertThat(dbOrder.get().getStatus()).isEqualTo(OrderStatus.EXECUTED);

        Optional<Execution> dbExecution = executionRepository.findByOrderId(response.getOrderId());
        assertThat(dbExecution).isPresent();

        OrderResponse loaded = orderService.getOrder(response.getOrderId());
        assertThat(loaded.getSymbol()).isEqualTo("AAPL");
        assertEquals(0,loaded.getExecutedPrice().compareTo(BigDecimal.valueOf(123.45)));
    }
}
