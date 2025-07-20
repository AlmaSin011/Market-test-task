package com.example.market.service;

import com.example.market.dto.response.OrderResponse;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IdempotencyService {

    private final Map<String, OrderResponse> responseCache = new ConcurrentHashMap<>();

    public boolean contains(String key) {
        return responseCache.containsKey(key);
    }

    public OrderResponse get(String key) {
        return responseCache.get(key);
    }

    public void save(String key, OrderResponse response) {
        responseCache.put(key, response);
    }
}
