# Food Delivery Service

A Spring Boot food-delivery order management API with in-memory or PostgreSQL-backed persistence, role-based access via request headers, simulated payments, and async order notifications.

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

Payment simulation mode (`payment.simulation.mode`):

- `ALWAYS_SUCCESS` (default in dev/test)
- `ALWAYS_FAIL`
- `RANDOM` (uses `payment.simulation.success-rate`, default `0.8`)

Clients can also pass `paymentScenario` (`SUCCEED` / `FAIL`) on place-order and retry-payment requests.

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
