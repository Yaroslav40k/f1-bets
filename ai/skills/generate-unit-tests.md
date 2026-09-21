---
name: generate-unit-tests
description: Generate JUnit 5 unit tests following this repo's testing standards. Loaded by implementation/review agents whenever a phase requires new tests.
---

# Skill: Generate Unit Tests

Follow `ai/standards/testing-standards.md` for the authoritative rules. This
skill is the step-by-step procedure.

## Procedure

1. Identify the unit under test (a single class/method) — one test class per
   production class, `{ClassName}Test.java`, mirroring the package under `src/test/java`.
2. Use JUnit 5 (`@Test`, `@ParameterizedTest`) + AssertJ assertions + Mockito
   for collaborators. No Hamcrest, no JUnit 4.
3. Structure every test method as **Arrange / Act / Assert**, separated by
   blank lines (no `// given/when/then` comments needed if the blocks are
   visually obvious).
4. **One logical assertion per test.** If you need to assert multiple
   properties of the same result object, use AssertJ's chained/`assertThat(...).satisfies(...)`
   rather than splitting into unrelated tests — but never test two unrelated
   behaviors in one method.
5. Name tests `methodUnderTest_condition_expectedResult()`.
6. Cover:
   - Happy path.
   - Every validation rule / invariant (from the entity or DTO builder).
   - Every typed exception the unit can throw, and the exact `MessageKeys` used.
   - Boundary values for anything with an explicit range/limit.
7. Use test data builders (`aValidOrder()`-style helpers) instead of
   constructing raw entities inline in every test.
8. Do not mock what you don't own only to satisfy compilation — mock actual
   collaborators (repositories, external clients), not value objects.
9. Run `./mvnw -q test -Dtest={ClassName}Test` to confirm before reporting.

## Output

A single `{ClassName}Test.java` file (or an addition to an existing one),
plus an updated entry in the feature's coverage mapping
(`docs/features/{slug}/02-design/04-testing.md`) if this test corresponds to
a planned coverage row.
