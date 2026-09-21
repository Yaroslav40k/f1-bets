# Module Map

> Template — populate once the package structure exists. Keep this in sync
> with `src/main/java/com/andersen/` — Research agents should update this
> file whenever they discover it's stale.

## Package layout convention

```
src/main/java/com/andersen/
  {module}/
    controller/     — @RestController classes, request/response mapping only
    service/         — business logic, orchestration, transactions
    repository/      — Spring Data JPA repositories + Specifications
    mapper/          — hand-written entity <-> DTO mappers
    model/           — @Entity JPA classes
    dto/             — request/response record types
    event/           — domain event payloads + publishers/listeners (Kafka)
    validation/       — custom validators
    config/          — @Configuration classes scoped to this module
  common/
    exception/       — ApplicationException hierarchy
    security/        — auth helpers, SecurityConfig
    config/          — cross-cutting Spring config
```

## Modules

| Module | Package | Owns | Depends on (via facade/event only) |
|---|---|---|---|
| [module-name] | `com.andersen.{module}` | [entities/responsibility] | [other modules] |

<!-- Repeat one row per module. -->

## Inter-module communication

Record here which modules talk to which, and how (Spring event vs. facade
bean) — see `standards/architecture-standards.md` for the rule (never direct
service injection across modules).

| From module | To module | Mechanism | Trigger |
|---|---|---|---|
| [module A] | [module B] | event / facade | [what triggers the call] |
