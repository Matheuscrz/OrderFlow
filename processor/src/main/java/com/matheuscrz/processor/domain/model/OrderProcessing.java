package com.matheuscrz.processor.domain.model;

import com.matheuscrz.processor.domain.model.enums.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;

import java.util.UUID;

import com.matheuscrz.processor.domain.model.abstractclass.BaseEntity;

@Getter
@Entity
@Table(name = "order_processing")
public class OrderProcessing extends BaseEntity {

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrderStatus status;

    protected OrderProcessing() {
    }

    public OrderProcessing(UUID orderId) {
        this.orderId = orderId;
        this.status = OrderStatus.CREATED;
    }

    public void changeStatus(OrderStatus status) {
        this.status = status;
    }
}