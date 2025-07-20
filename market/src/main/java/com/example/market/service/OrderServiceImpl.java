package com.example.market.service;

import com.example.market.client.PriceFeedClient;
import com.example.market.dto.request.OrderRequest;
import com.example.market.dto.response.OrderResponse;
import com.example.market.dto.response.PriceResponse;
import com.example.market.model.Execution;
import com.example.market.model.Order;
import com.example.market.model.OrderStatus;
import com.example.market.repository.ExecutionRepository;
import com.example.market.repository.OrderRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import io.micrometer.core.instrument.Counter;

import static com.example.market.utils.Constants.CREATED_TAG;
import static com.example.market.utils.Constants.METRIC_NAME;
import static com.example.market.utils.Constants.TAG_NAME;

@Service
public class OrderServiceImpl implements OrderService  {

    private final OrderRepository orderRepository;

    private final ExecutionRepository executionRepository;

    private final PriceFeedClient priceFeedClient;

    private final Counter orderCounter;

    public OrderServiceImpl(OrderRepository orderRepository,
                            ExecutionRepository executionRepository,
                            PriceFeedClient priceFeedClient,
                            MeterRegistry meterRegistry) {
        this.orderRepository = orderRepository;
        this.executionRepository = executionRepository;
        this.priceFeedClient = priceFeedClient;

        this.orderCounter = meterRegistry.counter(METRIC_NAME, TAG_NAME, CREATED_TAG);
    }

    @Override
    public OrderResponse placeOrder(OrderRequest request) {
        PriceResponse priceResponse = priceFeedClient.getPrice(request.getSymbol());

        Order order = Order.builder()
                .accountId(request.getAccountId())
                .symbol(request.getSymbol())
                .side(request.getSide())
                .quantity(request.getQuantity())
                .status(OrderStatus.EXECUTED)
                .createdAt(LocalDateTime.now())
                .build();

        Order savedOrder = orderRepository.save(order);

        Execution execution = Execution.builder()
                .order(order)
                .price(priceResponse.getPrice())
                .executedAt(order.getCreatedAt())
                .build();

        executionRepository.save(execution);
        orderCounter.increment();


        return OrderResponse.builder()
                .orderId(savedOrder.getId())
                .accountId(savedOrder.getAccountId())
                .symbol(savedOrder.getSymbol())
                .side(savedOrder.getSide().name())
                .quantity(savedOrder.getQuantity())
                .executedPrice(priceResponse.getPrice())
                .executedAt(savedOrder.getCreatedAt())
                .build();
    }

    @Override
    public OrderResponse getOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));

        Execution execution = executionRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new RuntimeException("Execution not found for order: " + id));

        return OrderResponse.builder()
                .orderId(order.getId())
                .accountId(order.getAccountId())
                .symbol(order.getSymbol())
                .side(order.getSide().name())
                .quantity(order.getQuantity())
                .executedPrice(execution.getPrice())
                .executedAt(execution.getExecutedAt())
                .build();
    }

    @Override
    public List<OrderResponse> getOrdersByAccount(String accountId) {

        List<Order> orders = orderRepository.findByAccountId(accountId);

        return orders.stream()
                .map(order -> {
                    Execution execution = executionRepository.findByOrderId(order.getId())
                            .orElseThrow(() -> new RuntimeException("Execution not found for order: " + order.getId()));

                    return OrderResponse.builder()
                            .orderId(order.getId())
                            .accountId(order.getAccountId())
                            .symbol(order.getSymbol())
                            .side(order.getSide().name())
                            .quantity(order.getQuantity())
                            .executedPrice(execution.getPrice())
                            .executedAt(execution.getExecutedAt())
                            .build();
                })
                .toList();
    }
}
