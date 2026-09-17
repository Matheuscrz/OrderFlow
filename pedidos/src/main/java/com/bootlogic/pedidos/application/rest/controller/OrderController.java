package com.bootlogic.pedidos.application.rest.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bootlogic.pedidos.application.rest.dto.OrderCreate;
import com.bootlogic.pedidos.application.rest.dto.OrderResponse;
import com.bootlogic.pedidos.application.rest.dto.OrderItemResponse;
import com.bootlogic.pedidos.domain.model.Order;
import com.bootlogic.pedidos.domain.model.OrderItem;
import com.bootlogic.pedidos.domain.ports.in.CreateOrderUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;

    @PostMapping
    public OrderResponse create(@RequestBody @Valid OrderCreate request) {
        Order order = toDomain(request);
        Order savedOrder = createOrderUseCase.execute(order);
        return toResponse(savedOrder);
    }

    private Order toDomain(OrderCreate request) {
        Order order = new Order();
        order.setCustomerId(request.customerId());

        request.items().forEach(item -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(item.productId());
            orderItem.setName(item.name());
            orderItem.setQuantity(item.quantity());
            orderItem.setPrice(item.price());
            order.addItem(orderItem);
        });
        return order;
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getProductId(),
                        item.getName(),
                        item.getQuantity(),
                        item.getPrice()))
                .toList();

        OrderResponse response = new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotalAmount(),
                items,
                order.getCreatedAt());
        return response;
    }
}
