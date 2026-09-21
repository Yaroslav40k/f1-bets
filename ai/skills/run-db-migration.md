---
name: run-db-migration
description: Create and apply a PostgreSQL schema migration following this repo's Flyway conventions.
---

# Skill: Run a Database Migration

See `ai/standards/database-standards.md` for the authoritative rules. This
skill is the step-by-step procedure.

## Procedure

1. Determine the next version number: check
   `src/main/resources/db/migration/` for the highest `V{n}__` prefix, increment by one.
2. Create the file:
   `src/main/resources/db/migration/V{n}__{snake_case_description}.sql`
   (e.g. `V42__add_orders_cancelled_at_column.sql`).
3. Write forward-only DDL. Never edit a previously-applied migration file —
   if a mistake shipped, write a new corrective migration.
4. Follow safe rollout ordering for anything that could break a running
   previous-version instance during a rolling deploy:
   - Adding a nullable column → safe in one migration.
   - Adding a `NOT NULL` column → three-step: add nullable → backfill via a
     follow-up migration or application code → add `NOT NULL` constraint in a
     later migration once backfill is confirmed complete.
   - Renaming/dropping a column → expand/contract: add new column, dual-write
     from application code for one release, migrate reads, then drop the old
     column in a later migration.
5. Naming conventions: tables `snake_case` plural (`orders`), columns
   `snake_case`, foreign keys `fk_{table}_{referenced_table}`, indexes
   `idx_{table}_{column(s)}`.
6. Apply and verify locally:
   ```bash
   docker compose -f local/docker/docker-compose.yml up -d
   ./mvnw flyway:migrate
   ./mvnw flyway:info   # confirm the new version shows "Success"
   ```
7. Update the corresponding JPA entity/repository and the feature's
   `06-repo-model.md` field mapping table if one exists.
8. Add a round-trip persistence test (save → reload → assert all fields
   preserved) for any new/changed entity fields.
