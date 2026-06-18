# Additional Rules for agents.md

## Technology Stack

Unless explicitly specified otherwise:

* Use Java 17+
* Use Spring Boot
* Use Maven
* Use Lombok
* Use SLF4J logging

Prefer Spring Boot conventions over custom implementations.

---

## Design Ownership

The requirements may be intentionally open-ended.

Do not immediately ask the user to clarify every missing detail.

Instead:

1. Identify reasonable assumptions.
2. Present those assumptions explicitly.
3. Explain why they are reasonable.
4. Ask for confirmation only when assumptions materially affect the design.

Example:

Assumption:
A bid can only be placed while an auction is ACTIVE.

Reason:
This keeps the lifecycle deterministic and avoids ambiguity.

---

## Scope Management

Always identify:

### Core Features

The minimum set required for a working solution.

### Nice-to-Have Features

Enhancements that can be implemented later.

Focus discussion and implementation on core features first.

Avoid spending time on optional enhancements unless explicitly requested.

---

## API First Thinking

For Spring Boot projects:

Before implementation discuss:

* REST endpoints
* Request DTOs
* Response DTOs
* Error responses

Keep APIs simple and intuitive.

Avoid exposing internal entities directly from controllers.

---

## Layered Architecture

Prefer the following structure:

controller/
dto/
model/
enums/
repository/
service/
exception/
strategy/
state/
mapper/
config/
util/

Only create folders that are actually needed.

---

## DTO Guidelines

Use DTOs for:

* API requests
* API responses

Do not expose entities directly through controllers.

Validation annotations should be placed on request DTOs.

Examples:

@NotNull
@NotBlank
@Positive

Only validate important business constraints.

---

## Assumptions Section

Before implementation create a dedicated section:

# Assumptions

List all assumptions agreed during design discussions.

This section should be included in:

* README
* Design summary
* Final implementation plan

---

## Tradeoff Discussion

Whenever proposing a design:

Mention:

* Why this approach was chosen
* Simpler alternative
* Why alternative was rejected

Keep this discussion concise.

---

## Concurrency Discussion

For any shared mutable state discuss:

* Race conditions
* Data consistency
* Locking strategy

Only introduce synchronization when required by the requirements.

Do not prematurely optimize.

---

## Error Handling

Provide:

* GlobalExceptionHandler
* Meaningful HTTP status codes
* Structured error responses

Example:

{
"errorCode": "AUCTION_NOT_FOUND",
"message": "Auction does not exist"
}

---

## Testing Strategy

Discuss testing before implementation.

Cover:

### Unit Tests

* Service layer
* Business rules
* Validation

### Integration Tests

* Controller layer
* End-to-end flows

Focus on critical business scenarios.

Do not test framework code.

---

## Documentation Requirements

After implementation generate:

### Design Summary

* Entities
* Services
* Repositories
* Patterns
* Assumptions
* Tradeoffs

### Manual Test Checklist

For each scenario:

Input

Expected Behavior

Expected Logs

---

## Code Quality Rules

Prefer:

* Readability
* Simplicity
* Explicit naming

Avoid:

* Deep inheritance hierarchies
* Premature abstractions
* Generic frameworks for simple problems
* Overuse of design patterns

Code should be interview-friendly and explainable in 2–3 minutes.

---

## Spring Boot Specific Rules

Controllers:

* Thin controllers
* No business logic

Services:

* Business logic only

Repositories:

* Persistence operations only

DTOs:

* API contract only

Entities:

* Domain state only

Maintain strict separation of concerns.

---

## Final Delivery Checklist

Before considering the solution complete verify:

* Requirements covered
* Assumptions documented
* Important failure cases handled
* Logging added
* Tests added
* Folder structure followed
* Code compiles
* No dead code
* No unused abstractions
* README updated
