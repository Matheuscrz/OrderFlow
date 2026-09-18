package com.matheuscrz.order.infrastructure.persistence.repository;

import org.springframework.stereotype.Repository;

import com.matheuscrz.order.domain.model.Order;
import com.matheuscrz.order.domain.ports.out.OrderRepositoryPort;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final SpringDataOrderRepository springDataOrderRepository;

    @Override
    public Order save(Order order) {
        return springDataOrderRepository.save(order);
    }

}
