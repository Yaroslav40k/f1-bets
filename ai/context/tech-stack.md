# Tech Stack

> Fill in the exact versions once the project is bootstrapped. Placeholders
> below reflect the stack agreed for this project as of its creation.

## Runtime & framework

| Component | Choice | Notes |
|---|---|---|
| Language | Java 25 (LTS-track) | Use modern language features (records, pattern matching, sealed interfaces) where they improve clarity; do not chase every preview feature. |
| Framework | Spring Boot 3.x | Constructor injection only (`@RequiredArgsConstructor` via Lombok), no field injection. |
| Build tool | Maven (`./mvnw`) | Wrapper committed to the repo — never require a globally installed Maven. |
| Database | PostgreSQL 17 | Accessed via Spring Data JPA + JPA `Specification` for dynamic queries. |
| Migrations | Flyway | See `standards/database-standards.md`. |
| Messaging | Apache Kafka | Spring Kafka (`spring-kafka`). See `standards/kafka-standards.md`. |
| Local infra | Docker Compose | `local/docker/docker-compose.yml` — Postgres + Kafka (+ any other local deps). |

## Key libraries

| Concern | Library | Notes |
|---|---|---|
| Mapping | Hand-written `@Component` mappers | No MapStruct — see `standards/architecture-standards.md`. |
| Boilerplate | Lombok | `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`, etc. — exact usage rules in `standards/code-style.md`. |
| Validation | Jakarta Bean Validation (`@Valid`) | Combined with value objects for business-rule invariants. |
| Testing | JUnit 5 + AssertJ + Mockito | No JUnit 4, no Hamcrest. See `standards/testing-standards.md`. |
| Integration testing | Testcontainers (Postgres, Kafka) | Prefer over embedded/in-memory fakes for parity with production. |
| API docs | springdoc-openapi | Generated from annotated controllers, not hand-written specs. |
| Static analysis | Checkstyle + SpotBugs (via `./mvnw verify`) | Config in `standards/checkstyle/`. |
| Coverage | JaCoCo | Runs as part of `./mvnw verify`. |
| Auth | Spring Security, JWT resource server | Adjust to the actual identity provider (Keycloak/Auth0/Cognito/etc.) once decided — update this file and `standards/architecture-standards.md` accordingly. |

## Placeholders to replace once the project exists

- Base package: `com.andersen` → replace repo-wide with the real group/artifact id.
- Identity provider specifics (issuer URI, claims mapping).
- Any additional modules/services not yet decided (search/cache/object storage).

## Ownership

This file is reviewed and updated by the team whenever a stack decision
changes — do not let it drift from `pom.xml` reality.
