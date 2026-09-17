package com.bootlogic.pedidos.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.bootlogic.pedidos.domain.model.abstractclass.BaseEntity;
import com.bootlogic.pedidos.domain.model.enums.OrderStatus;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor 
public class Order extends BaseEntity {
    @Column (nullable = false)
    private UUID customerId;

    @Enumerated (EnumType.STRING)
    @Column (nullable = false)
    private OrderStatus status = OrderStatus.CREATED;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @OneToMany (mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        calculateTotalAmount();
    }

    private void calculateTotalAmount() {
        this.totalAmount = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, (subtotal, amount) -> subtotal.add(amount));
    }
}
