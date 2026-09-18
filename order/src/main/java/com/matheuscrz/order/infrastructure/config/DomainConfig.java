package com.matheuscrz.order.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.matheuscrz.order.domain.ports.out.OrderEventPublisherPort;
import com.matheuscrz.order.domain.ports.out.OrderRepositoryPort;
import com.matheuscrz.order.domain.service.OrderDomainService;

@Configuration
public class DomainConfig {

    @Bean
    OrderDomainService orderDomainService(
            OrderRepositoryPort orderRepositoryPort,
            OrderEventPublisherPort orderEventPublisherPort) {
        return new OrderDomainService(orderRepositoryPort, orderEventPublisherPort);
    }
}
