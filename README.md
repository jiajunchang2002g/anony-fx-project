# Multi-Currency Payment Platform

A production-style Spring Boot backend for a fintech internship portfolio.

## Overview

This project models a multi-currency wallet system with secure user authentication, double-entry ledger accounting, FX conversion, and pluggable bank connector simulations.

Supported currencies:

- SGD
- USD
- EUR
- JPY

## Architecture

```text
Client
  -> REST Controllers
    -> Application Services
      -> Repositories / Transaction Boundaries
        -> PostgreSQL (users, wallets, transfers, ledger_entries, fx_rates, bank_requests)
        -> Redis (latest FX rate cache)
        -> Mock Bank Connectors (DBS, OCBC, UOB)

Security flow:
Client -> JWT filter -> UserDetailsService -> SecurityContext -> Controllers
```

## Database Schema Summary

- `users`: authenticated platform users and roles
- `wallets`: per-user wallets, one row per currency per user
- `transfers`: immutable transfer records with idempotency keys
- `ledger_entries`: double-entry debit and credit records for each transfer
- `fx_rates`: cached FX snapshots with expiry metadata
- `bank_requests`: audit trail for connector simulation calls

Key integrity rules:

- wallet balances are constrained by a unique user/currency pair
- transfers use immutable ledger entries instead of direct balance-only updates
- transfer idempotency keys are unique
- foreign keys preserve referential integrity across the ledger chain

## Setup

### Run infrastructure

```bash
docker compose up -d postgres redis
```

### Build and test

```bash
mvn test
```

If you are working inside this workspace without a local Maven install, run the same command with the repository wrapper from the sample project and set `JAVA_HOME` to your installed JDK.

### Run the application

```bash
mvn spring-boot:run
```

## API Examples

### Register

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"alice@example.com","password":"Password123"}'
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"alice@example.com","password":"Password123"}'
```

### Create a wallet

```bash
curl -X POST http://localhost:8080/api/wallets \
  -H 'Authorization: Bearer <jwt>' \
  -H 'Content-Type: application/json' \
  -d '{"currency":"SGD"}'
```

### Transfer funds

```bash
curl -X POST http://localhost:8080/api/transfers \
  -H 'Authorization: Bearer <jwt>' \
  -H 'Content-Type: application/json' \
  -d '{
    "sourceWalletId":"<source-wallet-id>",
    "targetWalletId":"<target-wallet-id>",
    "amount":25.000000,
    "idempotencyKey":"transfer-001",
    "note":"portfolio demo"
  }'
```

### Refresh FX rates as admin

```bash
curl -X POST 'http://localhost:8080/api/fx-rates/refresh?base=SGD' \
  -H 'Authorization: Bearer <admin-jwt>'
```

### Simulate a bank connector

```bash
curl -X POST http://localhost:8080/api/bank-connectors/DBS/simulate \
  -H 'Authorization: Bearer <jwt>' \
  -H 'Content-Type: application/json' \
  -d '{
    "requestType":"PAYMENT_INITIATION",
    "payload":"{\"amount\":100}",
    "accessToken":"dbs-token"
  }'
```

## Design Decisions

- `BigDecimal` is used for all monetary values to avoid floating-point drift.
- FX rates are cached in Redis and persisted in PostgreSQL for auditability.
- Transfers are protected by transaction boundaries, wallet locking, and idempotency keys.
- The ledger is double-entry: every transfer writes one debit and one credit entry.
- Bank integration is intentionally pluggable through a connector registry so real partners can be swapped in later.
- The mock bank connectors use a signed-request stub so the integration layer can evolve without changing the REST contract.

## Testing

The project includes:

- unit tests for FX rounding and transfer validation
- integration tests for auth, wallets, transfers, and ledger history
- a concurrency test that verifies duplicate idempotent transfer requests do not double-spend

## Notes

- An admin account is not hard-coded into the application. Use your own admin provisioning flow or seed data if you want to exercise admin-only endpoints manually.
- The project uses Spring Security with stateless JWT authentication.
