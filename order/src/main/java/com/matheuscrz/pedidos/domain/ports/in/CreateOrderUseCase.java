package com.matheuscrz.pedidos.domain.ports.in;

import com.matheuscrz.pedidos.domain.model.Order;

public interface CreateOrderUseCase {
    Order execute(Order order);
}
