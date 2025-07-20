package com.example.market.service;

import com.example.market.dto.request.OrderRequest;
import com.example.market.dto.response.OrderResponse;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    OrderResponse placeOrder(OrderRequest request);

    OrderResponse getOrder(UUID id);

    List<OrderResponse> getOrdersByAccount(String accountId);
}
