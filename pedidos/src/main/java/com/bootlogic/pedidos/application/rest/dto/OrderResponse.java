package com.bootlogic.pedidos.application.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.bootlogic.pedidos.domain.model.enums.OrderStatus;

public record OrderResponse(
    UUID id,
    UUID customerId,
    OrderStatus status,
    BigDecimal totalAmount,
    List<OrderItemResponse> items,
    LocalDateTime createdAt
) {

}
