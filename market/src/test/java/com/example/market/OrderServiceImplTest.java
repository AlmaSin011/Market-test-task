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
import com.example.market.service.OrderServiceImpl;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.example.market.utils.Constants.CREATED_TAG;
import static com.example.market.utils.Constants.METRIC_NAME;
import static com.example.market.utils.Constants.TAG_NAME;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderServiceImplTest {

    private OrderRepository orderRepository;
    private ExecutionRepository executionRepository;
    private PriceFeedClient priceFeedClient;
    private MeterRegistry meterRegistry;
    private Counter counter;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setup() {
        orderRepository = mock(OrderRepository.class);
        executionRepository = mock(ExecutionRepository.class);
        priceFeedClient = mock(PriceFeedClient.class);
        meterRegistry = mock(MeterRegistry.class);
        counter = mock(Counter.class);

        when(meterRegistry.counter(METRIC_NAME, TAG_NAME, CREATED_TAG)).thenReturn(counter);

        orderService = new OrderServiceImpl(orderRepository, executionRepository, priceFeedClient, meterRegistry);
    }

    @Test
    void testPlaceOrder_success() {
        OrderRequest request = OrderRequest.builder()
                .accountId("acc1")
                .symbol("AAPL")
                .side(OrderSide.BUY)
                .quantity(10)
                .build();

        PriceResponse priceResponse = new PriceResponse("AAPL",BigDecimal.valueOf(150));

        when(priceFeedClient.getPrice("AAPL")).thenReturn(priceResponse);

        Order savedOrder = Order.builder()
                .id(UUID.randomUUID())
                .accountId("acc1")
                .symbol("AAPL")
                .side(OrderSide.BUY)
                .quantity(10)
                .status(OrderStatus.EXECUTED)
                .createdAt(LocalDateTime.now())
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(executionRepository.save(any(Execution.class))).thenReturn(null);

        OrderResponse response = orderService.placeOrder(request);

        assertThat(response.getAccountId()).isEqualTo("acc1");
        assertThat(response.getSymbol()).isEqualTo("AAPL");
        assertThat(response.getQuantity()).isEqualTo(10);
        assertThat(response.getExecutedPrice()).isEqualTo(BigDecimal.valueOf(150));

        verify(counter).increment();
    }

    @Test
    void testGetOrder_success() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
                .id(orderId)
                .accountId("acc1")
                .symbol("GOOG")
                .side(OrderSide.SELL)
                .quantity(5)
                .createdAt(LocalDateTime.now())
                .build();

        Execution execution = Execution.builder()
                .order(order)
                .price(BigDecimal.valueOf(100))
                .executedAt(order.getCreatedAt())
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(executionRepository.findByOrderId(orderId)).thenReturn(Optional.of(execution));

        OrderResponse response = orderService.getOrder(orderId);

        assertThat(response.getSymbol()).isEqualTo("GOOG");
        assertThat(response.getExecutedPrice()).isEqualTo(BigDecimal.valueOf(100));
    }

    @Test
    void testGetOrder_orderNotFound() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    void testGetOrder_executionNotFound() {
        UUID id = UUID.randomUUID();
        Order order = Order.builder().id(id).build();

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(executionRepository.findByOrderId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Execution not found");
    }

    @Test
    void testGetOrdersByAccount_success() {
        UUID orderId = UUID.randomUUID();
        String accountId = "acc1";

        Order order = Order.builder()
                .id(orderId)
                .accountId(accountId)
                .symbol("ETH")
                .side(OrderSide.BUY)
                .quantity(10)
                .createdAt(LocalDateTime.now())
                .build();

        Execution execution = Execution.builder()
                .order(order)
                .price(BigDecimal.valueOf(2500))
                .executedAt(order.getCreatedAt())
                .build();

        when(orderRepository.findByAccountId(accountId)).thenReturn(List.of(order));
        when(executionRepository.findByOrderId(orderId)).thenReturn(Optional.of(execution));

        List<OrderResponse> responses = orderService.getOrdersByAccount(accountId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getSymbol()).isEqualTo("ETH");
    }
}
