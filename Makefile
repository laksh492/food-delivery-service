# Food delivery service — dev shortcuts
# Usage: make help
#
# Examples:
#   make run-jpa      # Postgres + Spring Boot (jpa profile)
#   make run-memory   # In-memory mode (no Postgres)
#   make db-reset     # Wipe Postgres volume and re-init schema

JAVA_HOME ?= $(shell /usr/libexec/java_home -v 22 2>/dev/null || /usr/libexec/java_home -v 17 2>/dev/null || /usr/libexec/java_home 2>/dev/null)
export JAVA_HOME

MVN := mvn
COMPOSE := docker compose
SPRING_JPA := $(MVN) spring-boot:run -Dspring-boot.run.profiles=jpa
SPRING_MEMORY := $(MVN) spring-boot:run

.DEFAULT_GOAL := help

.PHONY: help db-up db-down db-reset db-logs db-ps \
        compile test package \
        run-jpa run-memory run-jpa-only \
        smoke-user

help: ## Show available shortcuts
	@echo "Food delivery service — make shortcuts"
	@echo ""
	@grep -E '^[a-zA-Z0-9_-]+:.*##' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*## "}; {printf "  \033[36m%-16s\033[0m %s\n", $$1, $$2}'
	@echo ""
	@echo "JAVA_HOME=$(JAVA_HOME)"

db-up: ## Start PostgreSQL (docker compose up -d)
	$(COMPOSE) up -d

db-down: ## Stop PostgreSQL
	$(COMPOSE) down

db-reset: ## Stop Postgres, delete volume, restart (fresh schema)
	$(COMPOSE) down -v
	$(COMPOSE) up -d
	@echo "Waiting for Postgres..."
	@sleep 4
	$(COMPOSE) ps

db-logs: ## Tail Postgres logs
	$(COMPOSE) logs -f postgres

db-ps: ## Show container status
	$(COMPOSE) ps

compile: ## Compile (uses Java 17/22 via JAVA_HOME)
	$(MVN) -q compile

test: ## Run tests
	$(MVN) test

package: ## Build jar
	$(MVN) -DskipTests package

run-jpa: db-up ## Start Postgres, then Spring Boot with jpa profile
	@echo "JAVA_HOME=$(JAVA_HOME)"
	$(SPRING_JPA)

run-jpa-only: ## Spring Boot jpa profile (Postgres must already be up)
	@echo "JAVA_HOME=$(JAVA_HOME)"
	$(SPRING_JPA)

run-memory: ## Spring Boot in-memory mode (no Postgres)
	@echo "JAVA_HOME=$(JAVA_HOME)"
	$(SPRING_MEMORY)

smoke-user: ## POST a test customer (app must be running on :8080)
	curl -s -w "\nHTTP %{http_code}\n" -X POST http://localhost:8080/users \
		-H "Content-Type: application/json" \
		-d '{"name":"Test User","phone":"9999999999","role":"CUSTOMER","city":"BANGALORE"}'
