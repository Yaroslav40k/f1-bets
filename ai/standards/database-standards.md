# Database Standards (PostgreSQL)

## Migrations

- Tool: **Flyway**. Files in `src/main/resources/db/migration/`, named
  `V{n}__{snake_case_description}.sql`, strictly incrementing version numbers.
- Migrations are **forward-only and immutable** once merged to the target
  branch — never edit a previously-applied migration. A mistake gets a new
  corrective migration.
- See `skills/run-db-migration.md` for the step-by-step procedure, including
  safe rollout patterns (expand/contract) for `NOT NULL` columns and
  renames/drops during a rolling deployment.

## Naming

| Object | Convention | Example |
|---|---|---|
| Table | `snake_case`, plural | `orders`, `order_items` |
| Column | `snake_case` | `created_at`, `customer_id` |
| Primary key | `id` (UUID or bigint identity — pick one convention project-wide) | |
| Foreign key column | `{referenced_table_singular}_id` | `customer_id` |
| Foreign key constraint | `fk_{table}_{referenced_table}` | `fk_orders_customers` |
| Index | `idx_{table}_{column(s)}` | `idx_orders_customer_id` |
| Unique constraint | `uq_{table}_{column(s)}` | `uq_orders_external_ref` |

## Entity ↔ table mapping

- One `@Entity` per table; fields `private`, exposed through methods only
  (see `standards/architecture-standards.md`).
- Every entity field must round-trip through persistence without loss — see
  the round-trip test rule in `standards/testing-standards.md`.
- `@Version` for optimistic locking on any entity mutated concurrently by
  more than one code path (including via Kafka consumers).
- Prefer `Instant`/`LocalDate` column types over legacy `Date`/`Timestamp`.

## Queries

- Dynamic multi-field filtering: JPA `Specification` per aggregate, combined
  with `Pageable`. Do not let derived query method names
  (`findByAAndBAndCAndD...`) grow past 2–3 conditions — convert to a
  `Specification` instead.
- No string-concatenated JPQL/native SQL with interpolated values — always
  parameter binding (`:paramName`) or `Specification` predicates. String
  concatenation with user input is an automatic security reject (see
  `agents/review-security.md`).

## Schema evolution safety

- Additive, nullable changes (new nullable column, new table) are safe in a
  single migration and a single deploy.
- Anything that could break a currently-running previous-version instance
  during a rolling deploy must use the **expand/contract** pattern across
  multiple releases: add → dual-write/backfill → migrate reads → contract
  (drop old column/constraint) in a later release once nothing references it.
- Large backfills run as a separate, monitored migration/job — never as part
  of application startup.

## Local development

`docker compose -f local/docker/docker-compose.yml up -d` starts a local
PostgreSQL 17 instance matching production version. `./mvnw flyway:info`
confirms applied/pending migrations before running the app locally.
