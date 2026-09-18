package com.matheuscrz.order.domain.ports.out;

import com.matheuscrz.order.domain.model.Order;

public interface OrderEventPublisherPort {
    void publishOrderCreatedEvent(Order order);
}
