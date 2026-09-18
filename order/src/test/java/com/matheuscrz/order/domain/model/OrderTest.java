package com.matheuscrz.order.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.matheuscrz.order.domain.model.enums.OrderStatus;

class OrderTest {

    @Test
    void shouldCalculateTotalOrderValueCorrectly() {
        OrderItem keyboard = new OrderItem(
                UUID.randomUUID(), "Teclado", 2, new BigDecimal("100.00"), null);

        OrderItem mouse = new OrderItem(
                UUID.randomUUID(), "Mouse", 1, new BigDecimal("50.00"), null);

        Order order = new Order();
        order.setCustomerId(UUID.randomUUID());
        order.addItem(keyboard);
        order.addItem(mouse);

        assertEquals(new BigDecimal("250.00"), order.getTotalAmount());
    }

    @Test
    void shouldCalculateSingleItemFromApiPayload() {
        OrderItem item = new OrderItem(
                UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                "Teclado",
                1,
                new BigDecimal("199.90"),
                null);

        Order order = new Order(
                UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                OrderStatus.CREATED,
                null,
                List.of(item));

        assertEquals(new BigDecimal("199.90"), order.getTotalAmount());
    }

    @Test
    void shouldCalculateMultipleItemsCorrectly() {
        OrderItem item1 = new OrderItem(
                UUID.randomUUID(), "Teclado", 2, new BigDecimal("199.90"), null);

        OrderItem item2 = new OrderItem(
                UUID.randomUUID(), "Mouse", 3, new BigDecimal("50.00"), null);

        Order order = new Order(
                UUID.randomUUID(),
                OrderStatus.CREATED,
                null,
                List.of(item1, item2));

        assertEquals(new BigDecimal("549.80"), order.getTotalAmount());
    }

    @Test
    void shouldRejectZeroQuantity() {
        assertThrows(IllegalArgumentException.class, () -> new OrderItem(
                UUID.randomUUID(),
                "Teclado",
                0,
                new BigDecimal("199.90"),
                null));
    }

    @Test
    void shouldRejectNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () -> new OrderItem(
                UUID.randomUUID(),
                "Teclado",
                -1,
                new BigDecimal("199.90"),
                null));
    }

    @Test
    void shouldRejectNegativePrice() {
        assertThrows(IllegalArgumentException.class, () -> new OrderItem(
                UUID.randomUUID(),
                "Teclado",
                1,
                new BigDecimal("-10.00"),
                null));
    }

    @Test
    void shouldRejectEmptyOrder() {
        assertThrows(IllegalArgumentException.class, () -> new Order(
                UUID.randomUUID(),
                OrderStatus.CREATED,
                null,
                List.of()));
    }
}
