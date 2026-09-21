# Standards — Index

Always-on rules. Every phase (Design, Planning, Implementation, Review) must
comply — see `agents/review-architecture.md` and `agents/review-security.md`
for enforcement.

| File | Covers |
|---|---|
| [`code-style.md`](./code-style.md) | Formatting, naming, imports, Lombok — Google Java Style baseline + our overrides, enforced by `checkstyle/`. |
| [`architecture-standards.md`](./architecture-standards.md) | Layering, inter-module communication, transactions, exceptions. |
| [`testing-standards.md`](./testing-standards.md) | JUnit 5/AssertJ/Mockito conventions, coverage expectations. |
| [`api-standards.md`](./api-standards.md) | REST conventions, error response shape, auth. |
| [`database-standards.md`](./database-standards.md) | Flyway migrations, naming, safe schema evolution. |
| [`kafka-standards.md`](./kafka-standards.md) | Topic/consumer naming, delivery semantics, idempotency. |
| [`documentation-standards.md`](./documentation-standards.md) | Javadoc, comments, one-current-version rule. |
| [`checkstyle/`](./checkstyle) | Enforceable configs: `checkstyle.xml`, `import-control.xml`, `suppressions.xml`, `intellij-code-style.xml`. Wire `checkstyle.xml` into the Maven build (`maven-checkstyle-plugin`) so `./mvnw verify` enforces `code-style.md` automatically. |

## Origin

Derived from this team's prior Checkstyle/IntelliJ configuration
(`draft_ai_sources/code_standards/`), adapted and genericized for this
project, with Google Java Style filling any gaps not explicitly covered.
Where the two conflict, our own rules win.

## Changing a standard

A standards change is a normal PR. If a change affects `checkstyle/*.xml`,
run `./mvnw verify` against the current codebase first — either fix the
resulting violations in the same PR or add a scoped, justified entry to
`suppressions.xml`.
