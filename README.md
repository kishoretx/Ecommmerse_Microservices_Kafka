# TinyCorp Ecommerce Kafka Microservices Demo

A complete event-driven ecommerce reference system using Spring Boot, Kafka, Maven, H2, OpenAPI, and a live dashboard UI.

## What You Get

- Microservices architecture with clear service boundaries
- Kafka event choreography (`orders.created`, `payments.processed`, `orders.fulfilled`)
- H2 database per service
- OpenAPI + Swagger UI on every service
- Structured logging
- Dashboard UI with:
  - live metrics cards/charts
  - demo order generator
  - manual order placement
  - dynamic line items
  - per-line subtotal and cart total
  - stock validation before submit
- Hundreds of seed records (800+ total)
- Docker Compose full-stack startup with healthchecks

## Architecture

### Services

1. `common-events`
- Shared event contracts used by producer/consumer services

2. `product-service` (`8081`)
- Product catalog + inventory reservation
- Seeds 300 products

3. `order-service` (`8082`)
- Creates orders
- Calls product service to reserve inventory
- Produces `orders.created`
- Consumes `payments.processed`
- Produces `orders.fulfilled` on successful payment
- Seeds 180 historical orders

4. `payment-service` (`8083`)
- Consumes `orders.created`
- Simulates payment outcome
- Produces `payments.processed`
- Seeds 220 historical payments

5. `notification-service` (`8084`)
- Consumes payment + fulfillment events
- Stores notification audit logs
- Seeds 160 historical notifications

6. `dashboard-ui` (`8080`)
- Aggregates metrics from all services
- Provides order UI and operations dashboard

### Event Flow

1. User creates order via dashboard/API.
2. `order-service` reserves stock and emits `orders.created`.
3. `payment-service` processes and emits `payments.processed`.
4. `order-service` updates status; on success emits `orders.fulfilled`.
5. `notification-service` records notification logs.

## Tech Stack

- Java 17
- Spring Boot 3.3.x
- Spring Data JPA
- Spring Kafka
- H2 in-memory database
- Springdoc OpenAPI
- Thymeleaf + Chart.js
- Docker + Docker Compose

## Project Layout

```text
C:\00_INTERVIEW\TinyCorpLaptopTracker
  pom.xml
  docker-compose.yml
  Dockerfile.service
  .dockerignore
  run-infra.ps1
  run-services.ps1
  common-events/
  product-service/
  order-service/
  payment-service/
  notification-service/
  dashboard-ui/
```

## Prerequisites

1. JDK 17+
2. Maven 3.8+
3. Docker Desktop
4. PowerShell (for helper scripts)

## Run Options

### Option A: Full Stack in Docker (Recommended)

```powershell
cd C:\00_INTERVIEW\TinyCorpLaptopTracker
docker compose up --build -d
```

Check status:

```powershell
docker compose ps
```

Follow logs:

```powershell
docker compose logs -f
```

Stop stack:

```powershell
docker compose down
```

Reset everything (including volumes/networks):

```powershell
docker compose down -v
```

### Option B: Kafka in Docker + Services via Maven

```powershell
cd C:\00_INTERVIEW\TinyCorpLaptopTracker
docker compose up -d kafka kafka-ui
```

Then start services in separate terminals:

```powershell
mvn -pl product-service spring-boot:run
mvn -pl order-service spring-boot:run
mvn -pl payment-service spring-boot:run
mvn -pl notification-service spring-boot:run
mvn -pl dashboard-ui spring-boot:run
```

## URLs

- Dashboard UI: `http://localhost:8080`
- Kafka UI: `http://localhost:8090`
- Product service Swagger: `http://localhost:8081/swagger-ui.html`
- Order service Swagger: `http://localhost:8082/swagger-ui.html`
- Payment service Swagger: `http://localhost:8083/swagger-ui.html`
- Notification service Swagger: `http://localhost:8084/swagger-ui.html`
- Dashboard service Swagger: `http://localhost:8080/swagger-ui.html`

H2 Console endpoints:
- Product: `http://localhost:8081/h2-console`
- Order: `http://localhost:8082/h2-console`
- Payment: `http://localhost:8083/h2-console`
- Notification: `http://localhost:8084/h2-console`

## API Quick Reference

### Product Service

- `GET /api/products`
- `GET /api/products/{id}`
- `POST /api/products/{id}/reserve?quantity=2`
- `GET /api/products/metrics`

### Order Service

- `POST /api/orders`
- `GET /api/orders`
- `GET /api/orders/metrics`

Sample request:

```json
{
  "customerName": "Alex Carter",
  "customerEmail": "alex@example.com",
  "items": [
    { "productId": 1, "quantity": 1 },
    { "productId": 2, "quantity": 2 }
  ]
}
```

### Payment Service

- `GET /api/payments/metrics`

### Notification Service

- `GET /api/notifications/metrics`

### Dashboard Service

- `GET /api/dashboard/metrics`
- `GET /api/dashboard/products`
- `POST /api/dashboard/orders`
- `POST /api/dashboard/demo-orders?count=10`

## Dashboard User Guide

1. Open `http://localhost:8080`.
2. Use `Refresh Metrics` to reload cards and charts.
3. Use `Generate 10 Demo Orders` for traffic simulation.
4. In `Place Order`:
- enter customer details
- add/remove line items
- choose products and quantities
- review line subtotal + cart total
- submit order
5. On submit, dashboard validates stock before calling order-service.

## Kafka Topics

- `orders.created`
- `payments.processed`
- `orders.fulfilled`

## Seed Data

- Product service: 300 products
- Order service: 180 historical orders
- Payment service: 220 historical transactions
- Notification service: 160 notification logs

Total seeded data: 860+ records.

## Healthchecks (Docker Compose)

Compose now uses healthchecks and startup ordering:

- `kafka` healthcheck verifies broker API availability.
- Every Spring service healthcheck calls `/actuator/health`.
- `depends_on` uses `condition: service_healthy` so downstream services wait until dependencies are ready.

## Logging

Each service logs at:
- `root=INFO`
- `com.tinycorp.*=DEBUG`

Recommended:

```powershell
docker compose logs -f order-service payment-service notification-service
```

## Troubleshooting

1. Kafka UI shows broker unreachable
- Check `docker compose ps`
- Check `docker compose logs kafka`
- Ensure `kafka` is `Up (healthy)`

2. Service exits right after startup
- Check `docker compose logs <service>`
- Rebuild images: `docker compose up --build -d`

3. Port already in use
- Stop conflicting app or change port mapping in `docker-compose.yml`

4. Stale/broken environment after many changes
- Run:
  - `docker compose down -v`
  - `docker compose up --build -d`

5. Maven local run cannot resolve shared module
- Run: `mvn -pl common-events -am install -DskipTests`

## Helper Scripts

- `run-infra.ps1`: starts Kafka infra quickly
- `run-services.ps1`: starts services in separate PowerShell windows

## Suggested Next Enhancements

1. API Gateway + service discovery
2. Retry/DLT topics and idempotency keys
3. Outbox pattern for guaranteed publish
4. Testcontainers integration tests
5. OpenTelemetry tracing
6. AuthN/AuthZ (OAuth2/JWT)
