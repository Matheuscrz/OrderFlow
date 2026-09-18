package com.matheuscrz.order.domain.ports.in;

import com.matheuscrz.order.domain.model.Order;

public interface CreateOrderUseCase {
    Order execute(Order order);
}
