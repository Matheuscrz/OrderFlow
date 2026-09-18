package com.matheuscrz.order.infrastructure.messaging;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.matheuscrz.order.domain.model.enums.EventType;

public record OrderCreatedEvent(
                UUID eventId,
                EventType eventType,
                UUID orderId,
                UUID customerId,
                List<OrderItemEvent> items,
                BigDecimal totalAmount,
                LocalDateTime occurredAt) {

}
