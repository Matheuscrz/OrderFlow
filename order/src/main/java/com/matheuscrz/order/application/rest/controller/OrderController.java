package com.matheuscrz.order.application.rest.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.matheuscrz.order.application.rest.dto.OrderCreate;
import com.matheuscrz.order.application.rest.dto.OrderItemResponse;
import com.matheuscrz.order.application.rest.dto.OrderResponse;
import com.matheuscrz.order.domain.model.Order;
import com.matheuscrz.order.domain.model.OrderItem;
import com.matheuscrz.order.domain.ports.in.CreateOrderUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "order", description = "API para gerenciamento do ciclo de vida dos pedidos")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um novo pedido", description = "Recebe os dados do cliente e os itens para gerar um novo pedido no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos", content = @Content)
    })
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
