# Food Delivery Service

A Spring Boot food-delivery order management API with in-memory or PostgreSQL-backed persistence, role-based access via request headers, simulated payments, and async order notifications.

## Table of contents

- [Functional requirements](#functional-requirements)
  - [Users & access](#users--access)
  - [Admin](#admin)
  - [Restaurant discovery & menu](#restaurant-discovery--menu-public--owner)
  - [Ordering & payments](#ordering--payments-customer)
  - [Restaurant order handling](#restaurant-order-handling)
  - [Delivery](#delivery)
  - [Ratings](#ratings)
  - [Notifications](#notifications)
  - [Persistence & infrastructure](#persistence--infrastructure)
- [Skills & tech stack](#skills--tech-stack)
- [Requirements](#requirements)
- [Quick start (in-memory)](#quick-start-in-memory)
- [Quick start (PostgreSQL)](#quick-start-postgresql)
- [Configuration](#configuration)
- [Authentication](#authentication)
- [Example: smoke test](#example-smoke-test)
- [Tests](#tests)
- [Assumptions](#assumptions)
- [API overview](#api-overview)
- [Project layout](#project-layout)
- [Manual test checklist](#manual-test-checklist)

## Functional requirements

The system implements the following capabilities, grouped by actor and domain.

### Users & access

- Register users (`CUSTOMER`, `RESTAURANT_OWNER`, `DELIVERY_PARTNER`, `ADMIN`) with name, phone, role, and optional city.
- Role-based API access via `X-User-Id` and `X-Role` headers; endpoints reject missing, invalid, or mismatched credentials.
- Users can fetch their own profile; admins can fetch any user.

### Admin

- Create restaurants (city, owner, name, cuisines).
- Create delivery partners (user + city).
- List all restaurants.

### Restaurant discovery & menu (public / owner)

- Search restaurants by city with optional filters: name, cuisine, minimum rating, active flag; supports pagination and sort (`name`, `id`, `rating`).
- View restaurant details and full menu.
- Restaurant owners manage menu items: create, update, and patch stock levels.
- Owners view incoming orders for their restaurant.

### Ordering & payments (customer)

- Place an order with one or more menu items from a single restaurant.
- Simulated payment on placement; optional `paymentScenario` (`SUCCEED` / `FAIL`) per request (defaults to success).
- Stock is reserved atomically per item; overselling is prevented under concurrency.
- Order lifecycle state machine: `PENDING_PAYMENT` → `PLACED` or `PAYMENT_FAILED`; on failure, stock is released.
- Cancel orders in `PLACED` or `ACCEPTED` (stock released, payment refunded if applicable).
- Retry payment on `PAYMENT_FAILED` orders (re-reserves stock, re-charges).
- View own order by id.

### Restaurant order handling

- Accept orders in `PLACED` → `ACCEPTED` (triggers delivery-partner assignment offers).
- Reject orders in `PLACED` → `REJECTED` (stock released, payment refunded).
- Owner-only access enforced per restaurant.

### Delivery

- Partners list available assignments in their city (accepted, unassigned orders).
- Race-safe assignment acceptance: exactly one partner wins when multiple try concurrently.
- Partner updates delivery status: `PREPARING` → `OUT_FOR_DELIVERY` → `DELIVERED`.
- Partner is marked available again when order is delivered.

### Ratings

- Customer rates a delivered order once (restaurant stars required; partner stars required if a partner was assigned).
- Restaurant and delivery partner aggregate ratings (`ratingSum`, `reviewCount`) are updated.

### Notifications

- Async notifications on order status changes (customer, restaurant owner, assigned partner).
- Fan-out to eligible delivery partners when an order becomes assignable (`ACCEPTED`, same city).

### Persistence & infrastructure

- Pluggable repository layer: in-memory (default) or JPA/PostgreSQL (`jpa` profile).
- Optimistic locking on contested entities; structured error responses with HTTP status mapping.

## Skills & tech stack

- **Java 17+** — core language
- **Spring Boot 3** — Web, Validation, Data JPA, configuration properties
- **Maven** — build and dependency management
- **Lombok** — boilerplate reduction on entities and DTOs
- **SLF4J** — structured logging
- **PostgreSQL 16** — production persistence (via Docker Compose)
- **Spring Data JPA** — repositories, specifications, optimistic locking
- **H2** — in-memory database for JPA search tests
- **Docker / Docker Compose** — local Postgres setup
- **REST API** — thin controllers, request/response DTOs, global exception handling
- **JUnit 5 & MockMvc** — unit, integration, and concurrency tests
- **Design patterns** — State (order lifecycle), Repository (memory + JPA), Gateway (payment simulation)
- **Concurrency** — CAS stock reservation, race-safe partner assignment
- **Async events** — `ApplicationEventPublisher` with `@Async` notification listeners

## Requirements

- Java 17+
- Maven 3.9+
- Docker (optional, for PostgreSQL)

## Quick start (in-memory)

No database required. Data is stored in memory and resets on restart.

```bash
mvn spring-boot:run
```

Or use the Makefile:

```bash
make run-memory
```

The API listens on **http://localhost:8080**.

## Quick start (PostgreSQL)

### 1. Start Postgres

```bash
docker compose up -d
```

Or:

```bash
make db-up
```

This starts PostgreSQL 16 with:

| Setting   | Value            |
|-----------|------------------|
| Database  | `food_delivery`  |
| User      | `food_user`      |
| Password  | `food_pass`      |
| Port      | `5432`           |

Schema is applied automatically on startup (`schema.sql`).

### 2. Run the application with the JPA profile

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=jpa
```

Or:

```bash
make run-jpa
```

`make run-jpa` starts Postgres (if needed) and then launches the app with the `jpa` profile.

To run only the app when Postgres is already up:

```bash
make run-jpa-only
```

### Reset the database

```bash
make db-reset
```

Stops Postgres, deletes the volume, and recreates a fresh database.

## Configuration

| Profile | File                     | Repository | Database   |
|---------|--------------------------|------------|------------|
| default | `application.yml`        | in-memory  | none       |
| `jpa`   | `application-jpa.yml`    | JPA        | PostgreSQL |
| `test`  | `application-test.yml`   | in-memory  | none       |

Override datasource credentials with environment variables:

```bash
export DB_USER=food_user
export DB_PASS=food_pass
```

Payment outcome is controlled per request via optional `paymentScenario` on place-order and retry-payment (`SUCCEED` or `FAIL`). When omitted, payment succeeds.

## Authentication

Protected endpoints expect headers (no JWT in this project):

| Header      | Example        |
|-------------|----------------|
| `X-User-Id` | `1`            |
| `X-Role`    | `CUSTOMER`     |

Roles: `ADMIN`, `RESTAURANT_OWNER`, `CUSTOMER`, `DELIVERY_PARTNER`.

`POST /users` is open (user registration). All other secured routes validate that the user exists and the role matches.

## Example: smoke test

With the app running:

```bash
make smoke-user
```

Or manually:

```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","phone":"9999999999","role":"CUSTOMER","city":"BANGALORE"}'
```

## Tests

```bash
mvn test
```

Or:

```bash
make test
```

The suite includes:

- **Service unit tests** — `OrderService`, `RestaurantService`, `RatingService`
- **Concurrency tests** — stock reservation, partner assignment, payment retry
- **Controller integration tests** — MockMvc with in-memory profile (`test`)
- **JPA search tests** — `RestaurantSpecifications` against H2 (`jpa-test` profile)

## Assumptions

- Users are identified by integer IDs; authentication is header-based for interview/demo scope.
- Restaurants belong to a single city; delivery partners are matched by city.
- Stock is reserved at order placement (and re-reserved on payment retry); released on cancel/reject/payment failure.
- Only one rating per delivered order.
- Payment is simulated locally (no external gateway).

## API overview

| Area            | Base paths                                      |
|-----------------|-------------------------------------------------|
| Users           | `/users`                                        |
| Admin           | `/admin/restaurants`, `/admin/delivery-partners`|
| Restaurants     | `/restaurants`, `/cities/{city}/restaurants`    |
| Orders          | `/orders`                                       |
| Delivery        | `/delivery-partners`, `/orders/.../assignment`  |

See controller classes under `com.fooddelivery.controller` for the full endpoint list.

## Project layout

```
src/main/java/com/fooddelivery/
  config/          Spring configuration, auth interceptor
  controller/      REST API
  dto/             Request/response contracts
  enums/           Domain enums
  exception/       Error handling
  gateway/         Payment simulation
  model/           JPA entities
  notification/    Async event listeners
  repository/      Interfaces + in-memory + JPA implementations
  service/         Business logic
  state/           Order state machine
```

## Manual test checklist

| Scenario | Input | Expected |
|----------|-------|----------|
| Create customer | `POST /users` with `CUSTOMER` role | `201`, user returned with id |
| Place order | `POST /orders` with customer headers | `201`, status `PLACED` |
| Insufficient stock | Order quantity > available stock | `409 INSUFFICIENT_STOCK` |
| Missing auth | Secured route without headers | `403 ACCESS_DENIED` |
| Search restaurants | `GET /cities/BANGALORE/restaurants` | Paginated list |
| Accept assignment | Partner accepts unassigned order | Order `assignedPartnerId` set |
| Rate order | `POST /orders/{id}/ratings` after delivery | `201`, aggregates updated |
