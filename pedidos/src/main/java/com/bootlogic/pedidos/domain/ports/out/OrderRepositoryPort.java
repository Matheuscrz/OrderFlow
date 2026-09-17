package com.bootlogic.pedidos.domain.ports.out;

import com.bootlogic.pedidos.domain.model.Order;

public interface OrderRepositoryPort {
    Order save(Order order);
}
