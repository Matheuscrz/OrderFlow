package com.bootlogic.pedidos.application.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemCreate(
        @NotNull(message = "O ID do produto é obrigatório") UUID productId,
        @NotBlank (message = "O nome do produto é obrigatório") String name,
        @NotNull(message = "A quantidade do produto é obrigatória") @Positive(message = "A quantidade do produto deve ser um valor positivo") Integer quantity,
        @NotNull(message = "O preço do produto é obrigatório") @Positive(message = "O preço do produto deve ser um valor positivo") BigDecimal price) {

}
