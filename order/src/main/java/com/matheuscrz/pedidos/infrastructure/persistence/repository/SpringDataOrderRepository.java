package com.matheuscrz.pedidos.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.matheuscrz.pedidos.domain.model.Order;

public interface SpringDataOrderRepository extends JpaRepository<Order, UUID> {

}
