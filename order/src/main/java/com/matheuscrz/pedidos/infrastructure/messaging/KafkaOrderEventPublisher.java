package com.matheuscrz.pedidos.infrastructure.messaging;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.matheuscrz.pedidos.domain.model.Order;
import com.matheuscrz.pedidos.domain.model.enums.EventType;
import com.matheuscrz.pedidos.domain.ports.out.OrderEventPublisherPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaOrderEventPublisher implements OrderEventPublisherPort {

        private static final String TOPIC = "order-created";

        private final KafkaTemplate<String, Object> kafkaTemplate;

        @Override
        public void publishOrderCreatedEvent(Order order) {
                List<OrderItemEvent> itemEvents = order.getItems().stream()
                                .map(item -> new OrderItemEvent(
                                                item.getProductId(),
                                                item.getQuantity(),
                                                item.getPrice()))
                                .toList();

                OrderCreatedEvent event = new OrderCreatedEvent(
                                UUID.randomUUID(),
                                EventType.OrderCreated,
                                order.getId(),
                                order.getCustomerId(),
                                itemEvents,
                                order.getTotalAmount(),
                                LocalDateTime.now());

                kafkaTemplate.send(TOPIC, order.getId().toString(), event);

                log.info(
                                "Evento {} publicado com sucesso para o pedido {}",
                                event.eventType(),
                                order.getId());
        }
}
