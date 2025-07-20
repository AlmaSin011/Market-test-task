package com.example.market;

import com.example.market.dto.request.OrderRequest;
import com.example.market.model.OrderSide;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderControllerRateLimitTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void whenMoreThan10Requests_thenLastIsRateLimited() throws InterruptedException {
        String url = "/orders";

        OrderRequest request = new OrderRequest();
        request.setAccountId("test-account");
        request.setSymbol("AAPL");
        request.setSide(OrderSide.BUY);
        request.setQuantity(1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<OrderRequest> entity = new HttpEntity<>(request, headers);

        int successCount = 0;
        int rateLimitCount = 0;

        for (int i = 1; i <= 11; i++) {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                successCount++;
            } else if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                rateLimitCount++;
            }
        }
        log.info("Sucs count {} " , successCount);
        log.info("Rate limit count {} " , rateLimitCount);
        assertThat(successCount).isEqualTo(10);
        assertThat(rateLimitCount).isEqualTo(1);
    }
}
