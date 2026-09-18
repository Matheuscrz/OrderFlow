package com.matheuscrz.order.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.matheuscrz.order.domain.model.abstractclass.BaseEntity;
import com.matheuscrz.order.domain.model.enums.OrderStatus;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor
public class Order extends BaseEntity {

    @Column(nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.CREATED;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public Order(UUID customerId, OrderStatus status, BigDecimal totalAmount, List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("O pedido não pode ser vazio.");
        }
        
        this.customerId = customerId;
        this.status = status != null ? status : OrderStatus.CREATED;
        
        items.forEach(this::addItem);
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public void addItem(OrderItem item) {
        if (item == null) return;
        this.items.add(item);
        item.setOrder(this);
        calculateTotalAmount();
    }

    private void calculateTotalAmount() {
        this.totalAmount = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}