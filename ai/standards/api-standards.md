# API Standards

## Style

RESTful JSON over HTTP. Versioned base path: `/api/v1/{resource}`. Resource
names are plural, lowercase, kebab-case for multi-word resources
(`/api/v1/order-items`).

## Request/response conventions

- List endpoints return a page envelope:
  ```json
  { "items": [...], "total": 42, "page": 1, "limit": 20 }
  ```
- `page` defaults to 1, `limit` defaults to 20 with a max of 100 — reject
  (400) requests exceeding the max rather than silently clamping.
- Timestamps are ISO-8601 UTC strings (`"2024-01-01T00:00:00Z"`), backed by
  `java.time.Instant` — never epoch millis in the JSON contract.
- Request/response bodies are DTO `record` types (see `standards/architecture-standards.md`);
  every field's exact shape must be documented in the feature's
  `02-design/08-api-contract.md`.

## Error responses

Every error response follows one shape, produced by the single
`@ControllerAdvice GlobalExceptionHandler` — controllers never format errors themselves:

```json
{ "error": { "code": "ORD-004", "message": "Order cannot be cancelled after shipment" } }
```

| Status | Meaning | Source exception |
|---|---|---|
| 400 | Validation failed / malformed request | `MethodArgumentNotValidException`, custom validators |
| 401 | Missing/expired auth token | Spring Security |
| 403 | Authenticated but not authorized | `ForbiddenException`, `@PreAuthorize` |
| 404 | Resource not found | `ResourceNotFoundException` |
| 409 | Conflict / duplicate / invalid state transition | `ResourceAlreadyExistsException` |
| 502 | Upstream/external system failure | `ExternalSystemException` |
| 500 | Unhandled server error | anything else — should be rare; treat as a bug |

401/500 bodies may omit the `code` field (no business error code applies);
all others must include one from the module's registered range (see
`context/domain-glossary.md` → "Error code ranges").

## Documentation

Endpoints are documented via `springdoc-openapi` annotations on the
controller, kept in sync with the design's `08-api-contract.md` — see
`skills/write-openapi-spec.md` for the procedure.

## Auth

- Bearer JWT via Spring Security resource server. Every non-public endpoint
  has an explicit rule in `SecurityConfig` and/or `@PreAuthorize`.
- Authenticated principal is read via the security context, never a raw header.

## Idempotency & concurrency

- State-changing endpoints that are safe to retry (e.g. triggered by an
  at-least-once event) accept an idempotency key or are naturally idempotent
  by design — document which, in `08-api-contract.md`.
- Optimistic locking (`@Version`) is the default concurrency strategy for
  updatable entities exposed via `PUT`/`PATCH` — a 409 is returned on a
  version conflict, not a silent overwrite.

## Backward compatibility

- Adding a field to a response is non-breaking; removing or renaming one is
  breaking and requires a new API version or an explicit migration plan
  documented as an ADR in the feature's `03-decisions.md`.
