package com.matheuscrz.pedidos.application.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID productId,
        String name,
        Integer quantity,
        BigDecimal price) {

}
