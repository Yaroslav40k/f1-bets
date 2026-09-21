# Testing Standards

## Stack

JUnit 5 (`@Test`, `@ParameterizedTest`, `@Nested`) + AssertJ assertions +
Mockito for test doubles. No JUnit 4, no Hamcrest, no `org.junit.Assert.*`.

## Structure

- One test class per production class: `{ClassName}Test.java`, same package,
  under `src/test/java` (unit) or `src/it/java` (integration, if the module
  uses a separate integration-test source set).
- **Arrange / Act / Assert**, separated by a blank line. No inline narration
  comments needed if the three blocks are visually distinct.
- **One logical assertion per test method.** Asserting multiple properties of
  a single result object via AssertJ's fluent chaining counts as one logical
  assertion; asserting two unrelated behaviors does not — split those into
  separate tests.
- Test naming: `methodUnderTest_condition_expectedResult()`.
- Use test data builders (e.g. `TestOrders.aValidOrder()`) instead of
  constructing entities/DTOs inline in every test — keeps tests short and
  intention-revealing.

## What must be covered

Every entity, use case, and error code introduced by a feature must be
traceable to a test — see the coverage mapping table in
`docs/features/{slug}/02-design/04-testing.md`. At minimum, cover:

- Happy path.
- Every validation rule / invariant on entities and builders.
- Every typed exception a unit can throw, and the exact `MessageKeys` used.
- Boundary values for anything with an explicit numeric/length limit.
- State transitions: every transition in `01-architecture.md` needs both a
  test and (if user-facing) a sequence diagram in `02-behavior.md`.

## Relaxed rules inside test code

`standards/checkstyle/suppressions.xml` relaxes the following **only** under
`src/test` and `src/it`:
- `MagicNumber` — literal test data doesn't need named constants, though
  prefer them when the number's meaning isn't obvious from context.
- `AnnotationLocation`, `ExecutableStatementCount`, `VisibilityModifier` —
  test classes are naturally more verbose and don't need production-grade
  encapsulation.
- `InnerTypeLast` (unit tests only) — `@Nested` test classes may appear
  before helper methods for readability.

Everything else in `standards/code-style.md` still applies to test code.

## Integration tests

- Use **Testcontainers** for PostgreSQL and Kafka — prefer real containers
  over embedded/in-memory fakes to catch driver- and protocol-level issues
  that fakes hide.
- Integration tests verify the full stack for a slice (e.g. HTTP request →
  controller → service → repository → real Postgres), not every edge case —
  edge cases belong in fast unit tests.
- Kafka consumer tests: publish a real message to a test broker and assert
  the expected side effect; don't only unit-test the listener method in isolation.

## Mocking rules

- Mock real collaborators you don't want to exercise (repositories, external
  HTTP clients, Kafka producers) — don't mock value objects, records, or pure
  functions just to satisfy compilation.
- Prefer constructor-injected mocks (`@ExtendWith(MockitoExtension.class)`,
  `@Mock`/`@InjectMocks`) over `@MockBean` in unit tests — reserve
  `@MockBean`/`@SpringBootTest` for true integration tests, since they're
  significantly slower.

## Round-trip persistence tests

Any entity with a database migration must have at least one test that saves
the entity, reloads it, and asserts every field survived the round trip
unchanged (`Test{Entity}_RoundTrip_AllFieldsPreserved`) — this is how repo
model / entity mapping drift gets caught before production.

## Definition of done for tests

A phase is not complete until:
- [ ] `./mvnw -q test` passes for the touched module.
- [ ] `./mvnw -q verify` passes (JaCoCo coverage + static analysis).
- [ ] Every row in the feature's coverage mapping table has a corresponding,
      passing test.
