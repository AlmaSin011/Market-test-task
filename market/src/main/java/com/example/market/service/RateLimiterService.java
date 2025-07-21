package com.example.market.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private static final Integer COPASITY = 10;
    private static final Integer TOKENS = 10;

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String accountId) {
        return cache.computeIfAbsent(accountId, this::newBucket);
    }

    private Bucket newBucket(String accountId) {
        Bandwidth limit = Bandwidth.classic(COPASITY, Refill.intervally(TOKENS, Duration.ofSeconds(1)));

        return Bucket.builder().addLimit(limit).build();
    }
}
