# Architecture Standards

## Layering

```
controller  →  service  →  repository
                 │
                 ├──→ mapper (entity <-> dto)
                 └──→ model (JPA entity)
```

- `controller` (`@RestController`): request/response mapping only — parse
  input, call exactly one service method, map the result to a response DTO.
  **Never** call a repository directly from a controller.
- `service` (`@Service`): business logic and orchestration. Owns transaction
  boundaries. Coordinates repositories, mappers, other services *within the
  same module*, and publishes domain events for cross-module effects.
- `repository` (`@Repository`, Spring Data JPA): persistence only. Dynamic
  multi-field queries go through a JPA `Specification` class per aggregate,
  combined with `Pageable` — do not hand-write growing chains of
  `findByXAndYAndZ` derived query methods.
- `mapper`: plain `@Component` classes with explicit `toEntity()` / `toDto()`
  / `updateEntity()` methods. **No MapStruct, no reflection-based mapping
  libraries** — mapping bugs must be visible in a diff, not hidden in
  generated code.
- `model`: JPA `@Entity` classes. Fields are always `private`; invariants are
  enforced in constructors/builders, never left to callers to maintain.
- `dto`: Java `record` types (optionally `@Builder`/`@With` for partial
  construction/updates). **Never mutable DTO classes.**

`standards/checkstyle/import-control.xml` allows imports within
`com.andersen` broadly at the Checkstyle level; layering direction above
is enforced by **review** (`agents/review-architecture.md`), not by the
import-control tool — treat a controller importing a repository package as
an automatic reject.

## Inter-module communication

A "module" is a top-level package under `com.andersen` (see
`context/module-map.md`). Modules **never** inject another module's service
directly. Use one of:

1. **Spring application events** for fire-and-forget cross-module effects:
   ```java
   @Async
   @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
   public void on(OrderCancelledEvent event) { ... }
   ```
   Publish only after the originating transaction commits (see
   `skills/kafka-event-integration.md` for the same principle applied to Kafka).
2. **A `{Module}Facade` bean** for synchronous cross-module reads/writes that
   the caller needs a result from immediately. The facade is the *only*
   public entrypoint into a module from outside it — everything else in the
   module's `service`/`repository` packages is module-private.

## Transactions

- Class-level `@Transactional(readOnly = true)` on every `@Service`.
- Write methods override with method-level `@Transactional`.
- Cross-module event listeners that must run in their own transaction use
  `@Transactional(propagation = Propagation.REQUIRES_NEW)`.

## Exceptions

- All thrown exceptions are typed subclasses of a common `ApplicationException`:
  - `ResourceNotFoundException` → HTTP 404
  - `ResourceAlreadyExistsException` → HTTP 409
  - `ForbiddenException` → HTTP 403
  - `ExternalSystemException` → HTTP 502
- Every exception carries a `MessageKeys` constant resolved via `MessageSource`
  — never a hardcoded, ad-hoc string. This keeps error messages centralized,
  translatable, and greppable.
- All exceptions are translated to a response by a single
  `@ControllerAdvice` `GlobalExceptionHandler` — controllers never catch and
  format errors themselves.

## Time

Always `java.time.Instant` (for timestamps) or `java.time.LocalDate` (for
calendar dates) — never `java.util.Date`/`Calendar`.

## Security-adjacent conventions

- Authenticated user identity is read from the Spring Security
  `SecurityContextHolder` (via a small `TokenUtils`/`CurrentUserProvider`
  helper) — never from a raw request header.
- Authorization rules live declaratively in `SecurityConfig` and/or
  `@PreAuthorize` on service methods — every non-public endpoint must have an
  explicit rule (see `agents/review-security.md`).

## Database & messaging

See `standards/database-standards.md` and `standards/kafka-standards.md` for
the layer-specific rules (migrations, topic naming, idempotency).

## Anti-patterns that are automatic rejects

- Controller calling a repository directly.
- Any module injecting `{OtherModule}Service` directly instead of via
  facade/event.
- Mutable DTO classes, or DTOs reused as JPA entities (or vice versa).
- `RuntimeException`/generic `Exception` thrown or caught without a typed
  subclass and `MessageKeys`.
- Field injection (`@Autowired` on a field) instead of constructor injection.
- Business logic living in a controller or a mapper.
