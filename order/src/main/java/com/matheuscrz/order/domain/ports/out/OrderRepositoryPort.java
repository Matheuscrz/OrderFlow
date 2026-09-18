package com.matheuscrz.order.domain.ports.out;

import com.matheuscrz.order.domain.model.Order;

public interface OrderRepositoryPort {
    Order save(Order order);
}
