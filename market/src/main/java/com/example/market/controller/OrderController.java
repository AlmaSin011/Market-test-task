package com.example.market.controller;

import com.example.market.dto.request.OrderRequest;
import com.example.market.dto.response.OrderResponse;
import com.example.market.service.IdempotencyService;
import com.example.market.service.OrderService;
import com.example.market.service.RateLimiterService;
import io.github.bucket4j.Bucket;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    private final RateLimiterService rateLimiterService;

    private final IdempotencyService idempotencyService;

    @Operation(summary = "Place a new order",
            description = "Places a new order. Supports idempotency via X-Idempotency-Key header to prevent duplicate orders.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order placed successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE)),
            @ApiResponse(responseCode = "400", description = "Invalid order request",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<?> placeOrder(@Parameter(description = "Idempotency key to avoid duplicate requests", required = false)
                                        @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
                                        @Parameter(description = "Order request payload", required = true)
                                        @RequestBody @Valid OrderRequest request) {

        if (idempotencyKey != null && idempotencyService.contains(idempotencyKey)) {
            return ResponseEntity.ok(idempotencyService.get(idempotencyKey));
        }

        String accountId = request.getAccountId();
        Bucket bucket = rateLimiterService.resolveBucket(accountId);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Rate limit exceeded for accountId=" + accountId);
        }

        OrderResponse response = orderService.placeOrder(request);

        if (idempotencyKey != null) {
            idempotencyService.save(idempotencyKey, response);
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get order by ID",
            description = "Returns the details of an order by its UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@Parameter(description = "UUID of the order", required = true)
                                                  @PathVariable("id") UUID id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @Operation(summary = "Get orders by account ID",
            description = "Returns a list of orders for the specified account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of orders",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = OrderResponse.class, type = "array"))),
            @ApiResponse(responseCode = "400", description = "Missing or invalid accountId parameter",
                    content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrdersByAccount(@Parameter(description = "Account ID to filter orders", required = true)
                                                                  @RequestParam(name = "accountId") String accountId) {
        return ResponseEntity.ok(orderService.getOrdersByAccount(accountId));
    }
}
