package com.bootlogic.pedidos.domain.ports.in;

import com.bootlogic.pedidos.domain.model.Order;

public interface CreateOrderUseCase {
    Order execute(Order order);
}
