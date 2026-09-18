package com.matheuscrz.order.domain.service;

import com.matheuscrz.order.domain.model.Order;
import com.matheuscrz.order.domain.model.OrderItem;
import com.matheuscrz.order.domain.ports.out.OrderEventPublisherPort;
import com.matheuscrz.order.domain.ports.out.OrderRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderDomainServiceTest {

    @Mock
    private OrderRepositoryPort repositoryPort;

    @Mock
    private OrderEventPublisherPort eventPublisherPort;

    @InjectMocks
    private OrderDomainService domainService;

    private Order createValidOrder() {
        OrderItem item = new OrderItem(
                UUID.randomUUID(),
                "Monitor 24 polegadas",
                1,
                new BigDecimal("1500.00"),
                null
        );
        return new Order(
                UUID.randomUUID(),
                null,
                null,
                List.of(item)
        );
    }

    @Test
    void shouldExecuteOrderCreationSaveToRepositoryAndPublishEvent() {
        // Arrange
        Order order = createValidOrder();
        when(repositoryPort.save(any(Order.class))).thenReturn(order);

        // Act - Chama o método 'execute' conforme a interface CreateOrderUseCase
        Order savedOrder = domainService.execute(order);

        // Assert
        assertNotNull(savedOrder);
        verify(repositoryPort, times(1)).save(order);
        verify(eventPublisherPort, times(1)).publishOrderCreatedEvent(order);
    }

    @Test
    void shouldNotPublishEventWhenRepositoryFailsToSave() {
        // Arrange
        Order order = createValidOrder();
        // Simula uma falha no banco de dados (ex: constraint violation, timeout)
        when(repositoryPort.save(any(Order.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        // Verifica se a exceção sobe para a Controller corretamente
        assertThrows(RuntimeException.class, () -> domainService.execute(order));

        // Verifica que tentou salvar, mas falhou
        verify(repositoryPort, times(1)).save(order);
        // Garante que o evento NUNCA seja publicado se o pedido não foi persistido
        verify(eventPublisherPort, never()).publishOrderCreatedEvent(any());
    }
}