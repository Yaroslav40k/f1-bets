---
name: write-openapi-spec
description: Produce/update the OpenAPI 3 contract for REST endpoints touched by a feature, kept in sync with springdoc-generated docs.
---

# Skill: Write an OpenAPI Spec

## When to use

Whenever a Design (`08-api-contract.md`) or Plan phase introduces or changes
a REST endpoint.

## Procedure

1. This project uses `springdoc-openapi` — the spec is generated from
   annotated controllers, not hand-written from scratch. Annotate:
   - `@Operation(summary = ..., description = ...)` on each handler method.
   - `@ApiResponse(responseCode = "200", ...)` for every possible status code
     the endpoint can return (success and every error path from
     `standards/api-standards.md`).
   - `@Schema` on DTO record components where the field name alone isn't
     self-explanatory (constraints, example values, nullability).
2. Cross-check against `docs/features/{slug}/02-design/08-api-contract.md` —
   every documented endpoint, field, and error code in Design must be
   reflected in the annotations.
3. Generate and inspect the spec locally:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local &
   curl -s http://localhost:8080/v3/api-docs | jq . > /tmp/openapi.json
   ```
4. Diff the generated spec's paths/schemas against `08-api-contract.md`. Any
   mismatch is a bug — either the code or the design doc is wrong; fix the
   code to match Design, or flag the Design doc as outdated (never silently
   diverge).
5. Do not commit generated JSON/YAML spec files unless the project explicitly
   publishes them as a build artifact — the source of truth is the annotated
   code plus `08-api-contract.md`.
