package com.bootlogic.pedidos.application.rest.dto;

import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record OrderCreate(
                @NotNull(message = "O ID do cliente é obrigatório") UUID customerId,
                @NotEmpty(message = "A lista de itens do pedido não pode estar vazia") @Valid List<OrderItemCreate> items) {

}
