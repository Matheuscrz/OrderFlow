# OrderFlow

Projeto full stack desenvolvido com o objetivo de aprender e demonstrar, na prática, o uso do **Apache Kafka** em uma arquitetura orientada a eventos.

O sistema simula o processamento e rastreamento de pedidos de um pequeno e-commerce. Um pedido é criado através de uma API REST e, a partir desse momento, sua evolução é processada de forma assíncrona através de eventos Kafka.

O frontend acompanha as mudanças de status em **tempo real**, sem polling, através de WebSocket.

Todo o ambiente foi projetado para funcionar **100% localmente**, utilizando Docker Compose. Nenhum serviço de cloud é necessário para executar o projeto.

---

# 🎯 Objetivo do projeto

O objetivo principal não é criar um sistema completo de e-commerce.

O objetivo é utilizar um domínio pequeno e fácil de entender para aprender como o Kafka funciona dentro de uma aplicação real.

Durante o desenvolvimento serão explorados:

* Producer e Consumer
* Serialização e desserialização de eventos
* JSON como formato inicial de mensagens
* Topics
* Partitions
* Consumer Groups
* Offsets
* Chaves de mensagem
* Processamento assíncrono
* Eventos de domínio
* Desacoplamento entre componentes
* Event-driven architecture
* Eventual consistency
* Retry
* Dead Letter Topic
* **Idempotência** (via constraint no banco, resiliente mesmo sob concorrência)
* **Virtual Threads (Java 21)** aplicadas ao processamento dos consumers
* Escalabilidade de consumidores
* Comunicação Kafka → WebSocket
* Testes de integração com Kafka real

O projeto também servirá como laboratório para comparar posteriormente **Kafka e RabbitMQ**, principalmente em relação aos casos de uso de cada tecnologia.

---

# 💡 O que estamos construindo?

O sistema representa o ciclo de vida de um pedido.

Um cliente cria um pedido:

```text
Cliente
   ↓
"Quero comprar um teclado e um mouse"
```

O pedido começa com:

```text
CRIADO
```

Depois, o sistema processa automaticamente suas etapas:

```text
CRIADO
   ↓
PAGAMENTO_APROVADO
   ↓
SEPARACAO
   ↓
ENVIADO
   ↓
ENTREGUE
```

Cada mudança de estado gera um novo evento Kafka.

O frontend recebe essas mudanças através de WebSocket e atualiza a interface em tempo real.

O usuário não precisa atualizar a página.

---

# 🧠 O problema que Kafka resolve neste projeto

Uma implementação sem Kafka poderia ser:

```text
Frontend
    ↓
API
    ↓
Processa pagamento
    ↓
Atualiza pedido
    ↓
Atualiza histórico
    ↓
Envia notificação
```

Isso gera forte acoplamento.

A API precisaria conhecer diretamente todos os componentes envolvidos no processamento.

Com Kafka:

```text
                    ┌───────────────┐
                    │     Kafka     │
                    └───────┬───────┘
                            │
                   evento de pedido
                            │
                            ▼
                       Consumidores
```

O produtor publica um evento:

```text
"pedido criado"
```

e não precisa conhecer todos os consumidores.

Novos consumidores podem ser adicionados posteriormente sem modificar diretamente o produtor.

---

# 🏗️ Arquitetura

O projeto será dividido em três aplicações backend independentes e um frontend.

```text
                           ┌──────────────────────┐
                           │       FRONTEND       │
                           │   React + TypeScript │
                           └──────────┬───────────┘
                                      │
                                  HTTP / WS
                                      │
                ┌─────────────────────┴─────────────────────┐
                │                                           │
                ▼                                           ▼
       ┌────────────────┐                          ┌─────────────────┐
       │     pedidos    │                          │   ws-adapter    │
       │ Spring Boot    │                          │   Spring Boot    │
       │                │                          │                 │
       │ REST + Producer│                          │ Kafka → WS      │
       └───────┬────────┘                          └────────┬────────┘
               │                                           ▲
               │ pedido.criado                              │
               ▼                                           │
        ┌─────────────────────────────────────────────────────┐
        │                       KAFKA                         │
        │                     KRaft                           │
        │                                                     │
        │  pedidos.criado                                    │
        │  pedidos.status-atualizado                         │
        └──────────────┬───────────────────────┬──────────────┘
                       │                       │
                       ▼                       ▼
              ┌────────────────┐      ┌──────────────────┐
              │ processamento  │      │  ws-adapter      │
              │ Spring Boot    │      │ Kafka Consumer   │
              │                │      │ + WebSocket      │
              │ Consumer       │      └──────────────────┘
              │ State Machine  │
              │ Producer       │
              └───────┬────────┘
                      │
                      ▼
                 PostgreSQL
```

---

# 🧩 Componentes

## `pedidos`

Responsável pela entrada dos pedidos.

Principais responsabilidades:

```text
REST API
    ↓
Validar pedido
    ↓
Persistir pedido
    ↓
Publicar pedido.criado
```

Esse serviço será o principal **Kafka Producer** inicial.

Ele não será responsável por executar todo o processamento do pedido.

---

## `processamento`

Responsável por consumir pedidos criados e executar a máquina de estados.

Fluxo:

```text
Kafka
  ↓
pedido.criado
  ↓
processamento
  ↓
altera status
  ↓
publica pedido.status-atualizado
```

Esse serviço será:

```text
Consumer
+
Business Logic
+
Producer
```

O processamento dos eventos consumidos roda sobre **Virtual Threads**, o que permite lidar com múltiplos eventos concorrentes (e futuras chamadas de I/O bloqueante, como uma consulta a banco) sem consumir uma thread de plataforma por evento.

---

## `ws-adapter`

Responsável por fazer a ponte entre Kafka e o browser.

O navegador não irá consumir Kafka diretamente.

O fluxo será:

```text
Kafka
  ↓
ws-adapter
  ↓
WebSocket
  ↓
Frontend
```

Quando o status de um pedido mudar, o frontend recebe a atualização imediatamente.

---

## `frontend`

Aplicação React responsável por:

* criar pedidos;
* visualizar pedidos;
* acompanhar status;
* receber atualizações em tempo real;
* demonstrar visualmente o funcionamento dos eventos.

O frontend não terá qualquer dependência direta do Kafka.

---

# 📨 Eventos

O sistema utilizará inicialmente dois tipos principais de evento.

---

## `pedido.criado`

Publicado pelo serviço `pedidos`.

Topic:

```text
pedidos.criado
```

Exemplo:

```json
{
  "eventId": "5c0db1cb-79d4-4b40-9ca4-b2ce0f7e8a25",
  "eventType": "PEDIDO_CRIADO",
  "pedidoId": "1001",
  "clienteId": "42",
  "itens": [
    {
      "produtoId": "10",
      "nome": "Teclado",
      "quantidade": 1,
      "preco": 250.00
    },
    {
      "produtoId": "20",
      "nome": "Mouse",
      "quantidade": 1,
      "preco": 100.00
    }
  ],
  "valorTotal": 350.00,
  "occurredAt": "2026-09-13T20:00:00Z"
}
```

---

## `pedido.status-atualizado`

Publicado pelo serviço `processamento`.

Topic:

```text
pedidos.status-atualizado
```

Exemplo:

```json
{
  "eventId": "efb96958-0a79-48f7-a7a5-54e05b693b01",
  "eventType": "PEDIDO_STATUS_ATUALIZADO",
  "pedidoId": "1001",
  "statusAnterior": "CRIADO",
  "novoStatus": "PAGAMENTO_APROVADO",
  "occurredAt": "2026-09-13T20:00:05Z"
}
```

Esse evento será consumido simultaneamente por diferentes componentes.

---

# 🔑 Identificação dos eventos

Todo evento possuirá um identificador único:

```text
eventId
```

Esse campo será a base da estratégia de **idempotência** (ver seção dedicada abaixo).

Também será utilizada uma chave Kafka associada ao pedido:

```text
key = pedidoId
```

Exemplo:

```text
pedidoId = 1001
```

Isso permite manter os eventos de um mesmo pedido relacionados à mesma chave Kafka.

---

# 🔄 Fluxo completo

Considere a criação do pedido:

```http
POST /pedidos
```

Payload:

```json
{
  "clienteId": "42",
  "itens": [
    {
      "produtoId": "10",
      "quantidade": 1
    }
  ]
}
```

O serviço `pedidos` executa:

```text
1. Recebe requisição
2. Valida dados
3. Cria pedido
4. Persiste no banco
5. Publica PEDIDO_CRIADO
```

Kafka:

```text
pedidos.criado
```

O serviço `processamento` recebe:

```text
PEDIDO_CRIADO
```

Então executa a máquina de estados.

---

# 🔄 Máquina de estados

O pedido terá os seguintes estados:

```text
CRIADO
   ↓
PAGAMENTO_APROVADO
   ↓
SEPARACAO
   ↓
ENVIADO
   ↓
ENTREGUE
```

A progressão será simulada.

A ideia não é integrar inicialmente com gateway de pagamento ou transportadora.

O objetivo é demonstrar **processamento assíncrono**.

---

# 📏 Regras de negócio

## Regra 1 — criação

Todo pedido novo começa como:

```text
CRIADO
```

---

## Regra 2 — progressão

O pedido deve seguir a ordem:

```text
CRIADO
→ PAGAMENTO_APROVADO
→ SEPARACAO
→ ENVIADO
→ ENTREGUE
```

---

## Regra 3 — estados inválidos

Não será permitido avançar diretamente:

```text
CRIADO → ENVIADO
```

ou:

```text
SEPARACAO → ENTREGUE
```

---

## Regra 4 — pedido entregue

Um pedido com status:

```text
ENTREGUE
```

é considerado finalizado.

Nenhuma nova transição normal deverá ocorrer depois desse estado.

---

## Regra 5 — evento por transição

Toda mudança válida de status deve gerar:

```text
PEDIDO_STATUS_ATUALIZADO
```

---

## Regra 6 — histórico

Cada mudança de status deverá poder ser rastreada.

Exemplo:

```text
20:00:00 — CRIADO
20:00:05 — PAGAMENTO_APROVADO
20:00:10 — SEPARACAO
20:00:15 — ENVIADO
20:00:20 — ENTREGUE
```

---

## Regra 7 — idempotência

Um mesmo `eventId` nunca deve gerar duas transições de estado. Ver seção **🔐 Idempotência** para o mecanismo.

---

# 📡 Kafka Topics

Serão utilizados inicialmente dois topics.

| Topic                       | Produtor        | Consumidores    |
| --------------------------- | --------------- | --------------- |
| `pedidos.criado`            | `pedidos`       | `processamento` |
| `pedidos.status-atualizado` | `processamento` | `ws-adapter`    |

Posteriormente poderão ser adicionados:

```text
pedidos.dlq
```

para demonstrar Dead Letter Topic.

---

# 👥 Consumer Groups

O projeto utilizará Consumer Groups para demonstrar a distribuição do processamento.

## `processamento-group`

Consumirá:

```text
pedidos.criado
```

```text
pedidos.criado
        ↓
processamento-group
        ↓
processamento
```

---

## `ws-adapter-group`

Consumirá:

```text
pedidos.status-atualizado
```

```text
pedidos.status-atualizado
        ↓
ws-adapter-group
        ↓
WebSocket
```

---

# 🧪 Demonstração de escala

Um dos objetivos do projeto é observar o comportamento do Kafka quando existem múltiplas instâncias de um consumidor.

Por exemplo:

```text
pedidos.criado

Partition 0 ──────► processamento-1
Partition 1 ──────► processamento-2
Partition 2 ──────► processamento-3
```

As instâncias pertencem ao mesmo:

```text
Consumer Group
```

O Kafka distribuirá as partitions entre os consumidores do grupo.

Isso permitirá experimentar na prática conceitos que normalmente são difíceis de compreender apenas lendo documentação — inclusive o motivo pelo qual **Virtual Threads não substituem partitions**: elas resolvem concorrência dentro de uma instância, não distribuição de carga entre instâncias.

---

# 🧵 Partitions

O topic poderá ser criado inicialmente com:

```text
3 partitions
```

Exemplo:

```text
pedidos.criado

┌────────────┐
│ Partition 0│
└────────────┘

┌────────────┐
│ Partition 1│
└────────────┘

┌────────────┐
│ Partition 2│
└────────────┘
```

A chave será:

```text
pedidoId
```

Isso permitirá observar a relação entre:

```text
Key
↓
Partition
↓
Ordem dos eventos
```

---

# 🌐 Comunicação com o frontend

O navegador não consumirá Kafka diretamente.

O fluxo será:

```text
Kafka
  ↓
ws-adapter
  ↓
WebSocket
  ↓
Browser
```

Exemplo:

```text
pedido 1001
status = SEPARACAO
```

O Kafka publica:

```json
{
  "pedidoId": "1001",
  "novoStatus": "SEPARACAO"
}
```

O `ws-adapter` recebe:

```text
Kafka Consumer
```

e envia pelo WebSocket:

```json
{
  "pedidoId": "1001",
  "status": "SEPARACAO"
}
```

O React atualiza a tela.

---

# 🖥️ Interface

O frontend será propositalmente simples.

A tela principal mostrará algo próximo de:

```text
ORDER TRACKING
────────────────────────────────────────────

Pedido #1001

Cliente:
Matheus

Itens:
1x Teclado
1x Mouse

Status:

✓ CRIADO
✓ PAGAMENTO APROVADO
✓ SEPARAÇÃO
● ENVIADO
○ ENTREGUE

────────────────────────────────────────────

Última atualização:
18:32:17

```

A transição será visualizada automaticamente.

Por exemplo:

```text
CRIADO
   ↓
PAGAMENTO_APROVADO
```

sem atualizar a página.

---

# 📊 Dashboard

O frontend também poderá exibir:

```text
Pedidos
    25

Criados
    4

Em processamento
    8

Enviados
    7

Entregues
    6
```

Esses dados existem principalmente para tornar o comportamento do sistema visível.

---

# 💾 Persistência

O PostgreSQL será utilizado para persistir os dados necessários da aplicação.

Tabelas inicialmente planejadas:

```text
pedidos
pedido_itens
pedido_historico
processed_events
```

### `pedidos`

```text
id
cliente_id
status
valor_total
created_at
updated_at
```

### `pedido_itens`

```text
id
pedido_id
produto_id
nome
quantidade
preco
```

### `pedido_historico`

```text
id
pedido_id
status_anterior
novo_status
event_id
occurred_at
```

### `processed_events`

```text
event_id   (UNIQUE, chave da idempotência)
consumer   (qual serviço processou: "processamento" ou "ws-adapter")
processed_at
```

A persistência continuará simples.

O banco não tem como objetivo demonstrar arquitetura avançada de dados.

---

# 🔐 Idempotência

Consumidores devem evitar processar o mesmo evento duas vezes — inclusive sob concorrência real, já que múltiplos eventos podem estar sendo processados ao mesmo tempo em Virtual Threads distintas.

A estratégia adotada:

```text
1. Consumer recebe o evento
2. Tenta INSERT do eventId em processed_events (UNIQUE constraint)
3. Se o INSERT for bem-sucedido → processa o evento normalmente
4. Se o INSERT falhar por violação de unicidade → evento já processado, ignora
```

Isso é feito com `INSERT ... ON CONFLICT (event_id) DO NOTHING` no PostgreSQL, dentro da mesma transação que grava a mudança de status. A garantia fica no banco (constraint), não em uma checagem `if (exists) then skip` na aplicação — que teria condição de corrida quando duas Virtual Threads processam o mesmo `eventId` ao mesmo tempo (ex.: reprocessamento após rebalance).

Exemplo:

```text
eventId = 123

Thread A: INSERT event_id=123 → sucesso → processa
Thread B: INSERT event_id=123 → conflito → ignora
```

Esse comportamento será documentado e testado (inclusive com um teste que dispara o mesmo evento duas vezes propositalmente).

---

# ⚡ Virtual Threads

O projeto roda em **Java 21**, então os serviços backend usam **Virtual Threads** onde faz sentido, em vez de configurar um thread pool manual de tamanho fixo:

* **`pedidos`**: habilitado via `spring.threads.virtual.enabled: true` no `application.yml` — o Tomcat embutido passa a atender cada requisição HTTP em uma virtual thread.
* **`processamento`**: o listener container do Spring Kafka usa um `Executor` baseado em `Executors.newVirtualThreadPerTaskExecutor()` para o processamento de cada registro consumido, permitindo alta concorrência sem esgotar threads de plataforma — importante porque esse serviço fará I/O (banco) a cada evento.
* **`ws-adapter`**: mesma configuração de `spring.threads.virtual.enabled: true`, já que ele lida com múltiplas conexões WebSocket simultâneas.

Isso é uma escolha deliberada de aprendizado: o projeto serve também para sentir, na prática, a diferença entre escalar por **partitions/consumer groups** (paralelismo entre instâncias) e escalar por **Virtual Threads** (concorrência leve dentro de uma instância) — são mecanismos complementares, não substitutos um do outro.

---

# 🧩 Bounded Contexts

Embora o projeto seja pequeno, os componentes serão tratados como contextos com responsabilidades separadas.

### `pedidos`

Contexto responsável pelo recebimento e criação dos pedidos.

### `processamento`

Contexto responsável pela progressão do pedido.

### `ws-adapter`

Adapter responsável exclusivamente pela comunicação entre eventos Kafka e clientes WebSocket.

O objetivo é praticar a ideia de **baixo acoplamento** sem transformar o projeto em um conjunto excessivamente complexo de microserviços.

---

# 🛠️ Stack

| Categoria            | Tecnologia                            |
| --------------------- | -------------------------------------- |
| Linguagem             | Java 21                                |
| Concorrência          | Virtual Threads                        |
| Framework backend     | Spring Boot 3.2+                       |
| Kafka                 | Spring for Apache Kafka                |
| Mensageria            | Apache Kafka                           |
| Modo Kafka            | KRaft                                  |
| Banco                 | PostgreSQL                             |
| Frontend              | React                                  |
| Linguagem frontend    | TypeScript                             |
| Comunicação realtime  | WebSocket                              |
| Build backend         | Gradle (Groovy DSL)                    |
| Configuração          | `application.yml`                      |
| Containers            | Docker                                 |
| Orquestração local    | Docker Compose                         |
| Testes                | JUnit 5                                |
| Testes de integração  | Testcontainers                         |

---

# 📦 Dependências Gradle por serviço

## `pedidos` — `build.gradle`

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.3.x'
    id 'io.spring.dependency-management' version '1.1.x'
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.kafka:spring-kafka'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    runtimeOnly 'org.postgresql:postgresql'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.kafka:spring-kafka-test'
    testImplementation 'org.testcontainers:junit-jupiter'
    testImplementation 'org.testcontainers:kafka'
    testImplementation 'org.testcontainers:postgresql'
}
```

## `processamento` — `build.gradle`

Mesmas dependências de `pedidos` (Web só se expuser algum endpoint de consulta/health customizado — caso contrário, pode remover `spring-boot-starter-web` e manter apenas o Actuator).

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.kafka:spring-kafka'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    runtimeOnly 'org.postgresql:postgresql'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.kafka:spring-kafka-test'
    testImplementation 'org.testcontainers:junit-jupiter'
    testImplementation 'org.testcontainers:kafka'
    testImplementation 'org.testcontainers:postgresql'
}
```

## `ws-adapter` — `build.gradle`

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-websocket'
    implementation 'org.springframework.kafka:spring-kafka'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.kafka:spring-kafka-test'
}
```

Nenhum serviço precisa de `spring-boot-starter-security`, `RabbitMQ` ou `Redis` no MVP — mantém o classpath enxuto pro foco ficar no Kafka.

---

# ⚙️ `application.yml` — exemplo (`pedidos`)

```yaml
spring:
  application:
    name: pedidos
  threads:
    virtual:
      enabled: true
  datasource:
    url: jdbc:postgresql://postgres:5432/orderstream
    username: orderstream
    password: orderstream
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
  kafka:
    bootstrap-servers: kafka:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all

server:
  port: 8081
```

Cada serviço terá seu próprio `application.yml` com as chaves relevantes (o `processamento` adiciona a seção `spring.kafka.consumer`, o `ws-adapter` não precisa de `datasource`).

---

# 🐳 Infraestrutura local

Tudo será executado localmente.

```text
Docker Compose
│
├── kafka
├── postgres
├── order
├── process
├── ws-adapter
└── frontend
```

O Kafka será executado em modo:

```text
KRaft
```

sem ZooKeeper.

Não será necessário utilizar:

```text
Kafka Cloud
AWS
Azure
Google Cloud
Confluent Cloud
```

---

# 🚀 Como executar

## Pré-requisitos

Instalar:

```text
Docker
Docker Compose
Git
```

Java e Node.js não são obrigatórios caso todos os serviços sejam executados pelos containers.

---

# 🔁 Simulação completa

Ao criar um pedido, o sistema deverá executar aproximadamente:

```text
1. POST /pedidos
       ↓
2. Pedido salvo
       ↓
3. PEDIDO_CRIADO
       ↓
4. Kafka
       ↓
5. processamento recebe evento
       ↓
6. CRIADO → PAGAMENTO_APROVADO
       ↓
7. PEDIDO_STATUS_ATUALIZADO
       ↓
8. Kafka
       ↓
9. ws-adapter
       ↓
10. WebSocket
       ↓
11. React atualiza interface
```

Depois:

```text
PAGAMENTO_APROVADO
        ↓
SEPARACAO
        ↓
ENVIADO
        ↓
ENTREGUE
```

Cada etapa gera um evento.

---

# ⚠️ O que não fará parte inicialmente

Para manter o projeto pequeno, o MVP não terá:

```text
Gateway de pagamento real
Sistema de estoque real
Transportadora real
Login
Keycloak
Redis
RabbitMQ
Microservices complexos
CI/CD
```

Essas tecnologias podem aparecer futuramente, mas não fazem parte do objetivo inicial — o foco agora é código de desenvolvedor: producer, consumer, máquina de estados, idempotência e virtual threads.

---

# 🧪 Testes

Os testes serão incrementados conforme o projeto evolui.

## Testes unitários

Validar principalmente:

```text
Máquina de estados
Regras de transição
Validações
```

Exemplo:

```text
CRIADO → PAGAMENTO_APROVADO
```

deve ser válido.

Enquanto:

```text
CRIADO → ENVIADO
```

deve falhar.

---

## Testes de integração

O objetivo será testar:

```text
Spring Boot
+
PostgreSQL
+
Kafka
```

utilizando **Testcontainers**.

Testes importantes:

```text
Publicar PEDIDO_CRIADO
        ↓
Kafka
        ↓
Consumer
        ↓
Processamento
        ↓
PEDIDO_STATUS_ATUALIZADO
```

```text
Publicar o MESMO PEDIDO_CRIADO duas vezes
        ↓
Consumer processa a primeira
        ↓
Consumer ignora a segunda (idempotência)
```

Assim o projeto não dependerá de um mock simplificado para validar o Kafka.

---

# 🧯 Resiliência

Depois do fluxo básico funcionar, será adicionada uma camada simples de tratamento de falhas.

## Retry

Caso o consumidor falhe:

```text
Kafka
 ↓
Consumer
 ↓
Erro
 ↓
Retry
 ↓
Processa novamente
```

---

## Dead Letter Topic

Caso a mensagem não possa ser processada depois das tentativas:

```text
pedidos.criado
      ↓
Consumer
      ↓
Erro
      ↓
Retry
      ↓
Erro novamente
      ↓
pedidos.criado.DLT
```

Isso servirá para estudar uma característica importante de sistemas orientados a eventos:

> uma mensagem não deve simplesmente desaparecer porque seu processamento falhou.

---

# 📚 O que será estudado durante o projeto

## Kafka básico

```text
Broker
Topic
Producer
Consumer
Partition
Offset
```

## Kafka intermediário

```text
Consumer Groups
Message Keys
Partition Assignment
Ordering
Retries
Dead Letter Topic
```

## Arquitetura

```text
Event-driven architecture
Domain Events
Loose Coupling
Eventual Consistency
Asynchronous Processing
Idempotency
Virtual Threads
```

## Aplicação

```text
Kafka + Spring Boot
Kafka + PostgreSQL
Kafka + WebSocket
Kafka + React
Kafka + Testcontainers
Virtual Threads + Kafka Listener
```

---

# 📈 Evoluções futuras

Depois que o MVP estiver funcionando, o projeto poderá evoluir para demonstrar outros conceitos.

Possibilidades:

```text
Redis
Observabilidade (Prometheus, Grafana, OpenTelemetry)
Kafka UI
Autenticação
Outbox Pattern
Schema Registry
Avro
Retry Topics
Mais consumidores
Escalabilidade horizontal
```

Uma evolução particularmente interessante será implementar o **Transactional Outbox Pattern**.

Nesse cenário:

```text
Banco
  ↓
Outbox
  ↓
Publicação Kafka
```

será utilizado para estudar o problema:

```text
"Como garantir consistência entre banco de dados e publicação do evento?"
```

Essa funcionalidade não fará parte do MVP.

---

# 📁 Documentação do projeto

A documentação detalhada ficará em:

```text
docs/
├── architecture.md
├── business-rules.md
├── events.md
├── kafka.md
├── testing.md
└── decisions/
    ├── kafka-vs-rabbitmq.md
    ├── idempotencia.md
    ├── virtual-threads.md
    └── outbox-pattern.md
```

---

# 🎓 Resultado esperado

Ao finalizar o projeto, será possível demonstrar um fluxo completo:

```text
                    REST
                     │
                     ▼
              ┌─────────────┐
              │   pedidos   │
              └──────┬──────┘
                     │
              PEDIDO_CRIADO
                     │
                     ▼
              ┌─────────────┐
              │    Kafka    │
              └──────┬──────┘
                     │
                     ▼
              ┌─────────────┐
              │processamento│
              └──────┬──────┘
                     │
          STATUS_ATUALIZADO
                     │
                     ▼
              ┌─────────────┐
              │    Kafka    │
              └──────┬──────┘
                     │
                     ▼
              ┌─────────────┐
              │ ws-adapter  │
              └──────┬──────┘
                     │
                 WebSocket
                     │
                     ▼
              ┌─────────────┐
              │   React     │
              └─────────────┘
```

O sistema será pequeno, mas permitirá demonstrar conceitos importantes de engenharia de software:

```text
REST
Kafka
Event-driven architecture
Consumer Groups
Partitions
Asynchronous Processing
Virtual Threads
WebSocket
React
PostgreSQL
Docker
Testcontainers
Retry
Idempotency
Dead Letter Topics
```

---

# 📄 Licença

Este projeto está disponível sob a licença MIT.