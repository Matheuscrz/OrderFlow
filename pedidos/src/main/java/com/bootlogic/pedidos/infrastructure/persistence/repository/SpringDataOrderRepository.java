package com.bootlogic.pedidos.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bootlogic.pedidos.domain.model.Order;

public interface SpringDataOrderRepository extends JpaRepository<Order, UUID> {

}
