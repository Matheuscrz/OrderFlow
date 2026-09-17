package com.bootlogic.pedidos.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bootlogic.pedidos.domain.ports.out.OrderEventPublisherPort;
import com.bootlogic.pedidos.domain.ports.out.OrderRepositoryPort;
import com.bootlogic.pedidos.domain.service.OrderDomainService;

@Configuration
public class DomainConfig {

    @Bean
    OrderDomainService orderDomainService(
            OrderRepositoryPort orderRepositoryPort,
            OrderEventPublisherPort orderEventPublisherPort) {
        return new OrderDomainService(orderRepositoryPort, orderEventPublisherPort);
    }
}
