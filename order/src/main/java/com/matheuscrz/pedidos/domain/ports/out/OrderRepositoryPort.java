package com.matheuscrz.pedidos.domain.ports.out;

import com.matheuscrz.pedidos.domain.model.Order;

public interface OrderRepositoryPort {
    Order save(Order order);
}
