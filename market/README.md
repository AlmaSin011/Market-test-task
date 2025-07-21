# Mini Market Order Service

## Overview

Mini Market Order Service is a Spring Boot microservice for managing stock orders. It allows creating new orders, retrieving order details by UUID, and listing orders per account. Key features include:

- **Idempotency support** via `X-Idempotency-Key` header to prevent duplicate orders.
- **Rate limiting** per account using Bucket4j.
- **Request validation** to ensure data correctness.
- **Built-in health checks and metrics** for monitoring (Spring Boot Actuator + Prometheus).
- Lightweight Docker image optimized for cloud deployment.

---

## Prerequisites

- Java 21+ JDK
- Maven 3.9+
- Docker (optional, for containerized runs)
- PostgreSQL or configured via `application.yml`

---

## Build & Run

### Locally (without Docker)

```bash
git clone <repo-url>
cd mini-market-order
mvn clean package -DskipTests
java -jar target/mini-market-order-0.0.1-SNAPSHOT.jar


```

### In docker
```bash
docker-compose up -d
```
## Swagger
- Visit http://localhost:8080/swagger-ui.html for mini market
- Visit http://localhost:8081/swagger-ui.html for mock api 
