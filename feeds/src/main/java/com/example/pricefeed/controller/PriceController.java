package com.example.pricefeed.controller;

import com.example.pricefeed.dto.PriceResponse;
import com.example.pricefeed.service.PriceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/price")
public class PriceController {

    private final PriceService priceService;

    public PriceController(PriceService priceService) {
        this.priceService = priceService;
    }

    @GetMapping(produces = "application/json")
    @Operation(
            summary = "Get current price for a given symbol",
            description = "Returns the latest price for the specified symbol.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved price",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PriceResponse.class)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid symbol supplied"),
                    @ApiResponse(responseCode = "404", description = "Price not found for symbol")
            }
    )
    public ResponseEntity<PriceResponse> getPrice(@Parameter(description = "The symbol to get the price for", required = true, example = "BTCUSD")
                                                  @RequestParam(name = "symbol") String symbol) {
        BigDecimal price = priceService.getPrice(symbol);
        return ResponseEntity.ok(new PriceResponse(symbol.toUpperCase(), price));
    }
}
