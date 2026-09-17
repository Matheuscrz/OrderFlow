package com.bootlogic.pedidos.domain.service;

import com.bootlogic.pedidos.domain.model.Order;
import com.bootlogic.pedidos.domain.ports.in.CreateOrderUseCase;
import com.bootlogic.pedidos.domain.ports.out.OrderEventPublisherPort;
import com.bootlogic.pedidos.domain.ports.out.OrderRepositoryPort;

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