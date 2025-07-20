package com.example.pricefeed.service;

import java.math.BigDecimal;

public interface PriceService {

    BigDecimal getPrice(String symbol);
}
