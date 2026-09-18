package com.matheuscrz.order.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

import com.matheuscrz.order.domain.model.abstractclass.BaseEntity;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    public OrderItem(UUID productId, String name, Integer quantity, BigDecimal price, Order order) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O preço não pode ser negativo.");
        }

        this.productId = productId;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.order = order;
    }

    void setOrder(Order order) {
        this.order = order;
    }
}