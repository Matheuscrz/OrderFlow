package com.matheuscrz.order.domain.service;

import com.matheuscrz.order.domain.model.Order;
import com.matheuscrz.order.domain.ports.in.CreateOrderUseCase;
import com.matheuscrz.order.domain.ports.out.OrderEventPublisherPort;
import com.matheuscrz.order.domain.ports.out.OrderRepositoryPort;

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