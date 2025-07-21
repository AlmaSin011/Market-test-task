package com.example.market.dto.request;

import com.example.market.model.OrderSide;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

    @NotBlank
    private String accountId;

    @NotBlank
    private String symbol;

    @NotNull
    private OrderSide side;

    @Min(value = 1, message = "Should be > 0")
    private int quantity;
}
