# OrderFlow: E-commerce Platform

> Uma plataforma de e-commerce construída para demonstrar maturidade em engenharia de software, arquitetura de sistemas distribuídos, resiliência e segurança em profundidade.

O OrderFlow evoluiu de um simples laboratório de Kafka para uma arquitetura baseada em microserviços pronta para produção. O foco deste projeto **não é** reinventar a roda com abstrações complexas, mas sim aplicar as ferramentas certas para os problemas certos, seguindo princípios de **Domain-Driven Design (DDD)**, **pragmatismo** e **soluções nativas**.

## 🎯 Objetivo do Projeto

Demonstrar domínio absoluto sobre os desafios reais de sistemas distribuídos corporativos:
*   **Consistência de Dados:** Resolução de falhas silenciosas de dual-write utilizando o **Transactional Outbox Pattern**.
*   **Segurança (Zero Trust):** Autenticação robusta (Argon2id, JWT HS256), Blacklist de tokens distribuída, Rate Limiting e fluxo de upload seguro com scan assíncrono.
*   **Mensageria Híbrida:** Separação clara de responsabilidades entre **Apache Kafka** (Eventos de Domínio e core business) e **RabbitMQ** (Workflows assíncronos, auditoria e DLQs).
*   **Resiliência e Escalabilidade:** Idempotência e Distributed Locks (Redis), concorrência leve com **Java 21 Virtual Threads**.
*   **Qualidade:** Testes de integração infalíveis utilizando **Testcontainers** (bancos e brokers reais, sem mocks em memória).

## 🛠️ Stack Tecnológica

*   **Java 21 & Spring Boot 3.x:** Core da aplicação (tirando proveito de Virtual Threads para I/O escalável).
*   **PostgreSQL:** Banco de dados relacional principal. Utilizado com `FOR UPDATE SKIP LOCKED` para viabilizar o Outbox de forma nativa e concorrente.
*   **Apache Kafka (KRaft):** Broker de eventos do domínio. Garante a máquina de estados do pedido (CRIADO -> PAGO -> ENVIADO) mantendo o histórico e a ordem (particionamento por `pedidoId`).
*   **RabbitMQ:** Broker de tarefas e roteamento. Usado para envios de e-mail, logs de auditoria e fila de escaneamento de arquivos (Fire-and-forget).
*   **Redis (Redisson/Bucket4j):** Armazenamento de estado efêmero. Responsável por Rate Limiting distribuído, Distributed Locks, Idempotência e Blacklist de JWT.
*   **MinIO (S3 Compatible):** Storage de objetos. Armazena imagens de produtos e avatares com política de quarentena.
*   **React + TypeScript:** Frontend consumindo as APIs e recebendo atualizações de status via WebSocket.

## 📁 Estrutura da Documentação

Para entender a fundo como a plataforma foi desenhada e como implementá-la, consulte os guias detalhados:

1.  [Arquitetura e Microserviços (ARCHITECTURE.md)](ARCHITECTURE.md): Diagramas de fluxo, comunicação Kafka vs RabbitMQ e design do Outbox.
2.  [Roadmap de Implementação (IMPLEMENTATION_ROADMAP.md)](IMPLEMENTATION_ROADMAP.md): O passo a passo (Requisitos, Épicos e Tarefas) para construir a plataforma.

## 🚀 Como Executar Localmente

Todo o ecossistema roda de forma autocontida via Docker Compose, sem dependência de cloud externa.

```bash
# Sobe a infraestrutura (Postgres, Kafka, RabbitMQ, Redis, MinIO)
docker-compose -f infra/docker-compose.yml up -d

# Sobe os microserviços após a infra estar health
# (Comandos gradle omitidos para brevidade)
```