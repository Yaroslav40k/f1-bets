---
name: design
description: Design a feature using C4 (context/container/component), Data Flow, Sequence diagrams and ADRs. Human + architect-reviewer approval required before planning starts.
argument-hint: [feature-slug] [service/module path]
---

# Design Command

Uses persona: `ai/agents/design.md`.

**Core principle:** Design WHAT and WHY before HOW. No implementation
details until this phase is approved.

## Phase 0: Understand the mission

1. Parse arguments: `$ARGUMENTS[0]` feature slug, `$ARGUMENTS[1]` module path
   (e.g. `src/main/java/com/andersen/{module}`).
2. Read `docs/features/{feature-slug}/01-research.md` if it exists.
3. Read ALL of `ai/standards/` and `ai/context/` — especially
   `architecture-standards.md`, `api-standards.md`, `database-standards.md`,
   `kafka-standards.md`, `tech-stack.md`, `module-map.md`.
4. Decide which conditional documents apply (see table below).

| Condition | Conditional document |
|---|---|
| Feature emits/consumes Kafka events | `05-events.md` |
| Feature has new/changed DB entities | `06-repo-model.md` |
| Any backend feature | `07-standards.md` |
| Feature exposes REST endpoints | `08-api-contract.md` |

## Phase 1: Output structure

```
docs/features/{feature-slug}/02-design/
  README.md            — index, business context, acceptance criteria
  01-architecture.md    — C4 L1 (context) + L2 (container) + L3 (component)
  02-behavior.md        — Data Flow Diagrams + Sequence diagrams
  03-decisions.md       — ADRs, risks, open questions
  04-testing.md         — test strategy + coverage mapping (entities, error codes)
  05-events.md          — Kafka events (conditional)
  06-repo-model.md      — entity ↔ table mapping strategy (conditional)
  07-standards.md       — standards compliance matrix (conditional)
  08-api-contract.md    — exact REST request/response JSON shapes (conditional)
```

Use Mermaid for every diagram. All diagrams tell one continuous "zoom-in"
story in `01-architecture.md` (L1 → L2 → L3).

## Phase 2: Architect review

Run a review against the design using the criteria in `ai/agents/design.md`
("Self-review before handing off") plus:

- Cross-document consistency: every entity has test coverage in
  `04-testing.md`; every use case has a sequence in `02-behavior.md`; every
  error code is covered in both `02-behavior.md` and `04-testing.md`; every
  REST endpoint has exact JSON shapes in `08-api-contract.md`.
- No conflicts with error code ranges already in use (from `01-research.md`).

Produce:

```markdown
## Architecture Review: {Feature Name}
### Compliance
| Standard | Status | Notes |
### Cross-Document Consistency
| Check | Status | Details |
### Findings
❗ Critical / Important / Suggestions
### Verdict
✅ READY FOR REVIEW / ⚠️ NEEDS ITERATION
```

Iterate until ✅. Fix findings in the specific file where the issue lives —
never rewrite everything for a small findable issue.

## Phase 3: Human approval

Present a summary (business context, architecture highlights, review verdict,
document list) and **WAIT for explicit approval**. Do not proceed to
`commands/plan.md` without it. If changes are requested, update only the
specific file affected, re-run the architect review if the change is
significant, and present again.
