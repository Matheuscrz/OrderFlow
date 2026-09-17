package com.bootlogic.pedidos.domain.ports.out;

import com.bootlogic.pedidos.domain.model.Order;

public interface OrderEventPublisherPort {
    void publishOrderCreatedEvent(Order order);
}
