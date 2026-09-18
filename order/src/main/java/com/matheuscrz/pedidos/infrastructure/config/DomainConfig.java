package com.matheuscrz.pedidos.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.matheuscrz.pedidos.domain.ports.out.OrderEventPublisherPort;
import com.matheuscrz.pedidos.domain.ports.out.OrderRepositoryPort;
import com.matheuscrz.pedidos.domain.service.OrderDomainService;

@Configuration
public class DomainConfig {

    @Bean
    OrderDomainService orderDomainService(
            OrderRepositoryPort orderRepositoryPort,
            OrderEventPublisherPort orderEventPublisherPort) {
        return new OrderDomainService(orderRepositoryPort, orderEventPublisherPort);
    }
}
