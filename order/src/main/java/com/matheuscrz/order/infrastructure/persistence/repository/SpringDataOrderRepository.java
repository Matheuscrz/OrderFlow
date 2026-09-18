package com.matheuscrz.order.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.matheuscrz.order.domain.model.Order;

public interface SpringDataOrderRepository extends JpaRepository<Order, UUID> {

}
