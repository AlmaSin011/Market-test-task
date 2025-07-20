package com.example.market.client;

import com.example.market.dto.response.PriceResponse;
import com.example.market.exception.PriceFeedUnavailableException;
import com.example.market.exception.SymbolNotSupportedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PriceFeedClient {

    @Value("${price-feed.base-url}")
    private String feedUrl;

    private final RestTemplate restTemplate;

    @Retryable(
            value = {RestClientException.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 500)
    )
    public PriceResponse getPrice(String symbol) {
        String url = String.format("%s/price?symbol=%s", feedUrl, symbol);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<PriceResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    PriceResponse.class);

            return response.getBody();
        }
        catch (HttpClientErrorException.NotFound e) {
            throw new SymbolNotSupportedException("Symbol not found: " + symbol); // 422
        } catch (HttpServerErrorException e) {
            log.warn("Server error from price feed: {} - retrying...", e.getStatusCode());
            throw e; // try retry
        }
        catch (HttpClientErrorException e) {
            log.error("Client error from price feed: {}", e.getStatusCode());
            throw new PriceFeedUnavailableException("Price feed client error: " + e.getStatusCode());
        }
    }

    @Recover
    public PriceResponse recover(HttpServerErrorException ex, String symbol) {
        log.error("Price feed unavailable after retry for symbol {}", symbol);
        throw new PriceFeedUnavailableException("Price feed temporarily unavailable, try again later.");
    }
}
