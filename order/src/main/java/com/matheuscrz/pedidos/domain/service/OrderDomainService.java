package com.matheuscrz.pedidos.domain.service;

import com.matheuscrz.pedidos.domain.model.Order;
import com.matheuscrz.pedidos.domain.ports.in.CreateOrderUseCase;
import com.matheuscrz.pedidos.domain.ports.out.OrderEventPublisherPort;
import com.matheuscrz.pedidos.domain.ports.out.OrderRepositoryPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderDomainService implements CreateOrderUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final OrderEventPublisherPort orderEventPublisherPort;

    @Override
    public Order execute(Order order) {
        Order savedOrder = orderRepositoryPort.save(order);
        orderEventPublisherPort.publishOrderCreatedEvent(savedOrder);
        return savedOrder;
    }
}