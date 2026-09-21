# AI Development Artefacts

This folder is the single source of truth for how AI coding agents work in this
repository. It is versioned like code: changes go through a normal pull request
and are reviewed like any other change.

> Methodology reference: `How__to_develop_with_AI_General_rules.pdf`
> ("How to Develop with AI — The Andersen Rulebook for Developers"). This folder
> is the practical implementation of that methodology for a
> **Java 25 / Spring Boot / PostgreSQL / Kafka** codebase.

## Entry point for any agent

If you are an AI agent starting work in this repository, read this file first,
then jump to the phase you're in. Never skip a phase. Never start Implementation
without an approved Plan. Never start Planning without an approved Design.

## The four phases

Every non-trivial task goes through four phases, in strict order, with a
human approval gate between each one:

```
1. RESEARCH        →  2. DESIGN         →  3. PLANNING       →  4. IMPLEMENTATION
   (ticket → branch     (how it will look)    (steps to build)     (write the code)
    + what exists)
   agents/research.md    agents/design.md      agents/planning.md   agents/implementation.md
   commands/research.md  commands/design.md   commands/plan.md     commands/implement.md
```

Research also does the ticket bookkeeping: given a raw Jira ticket (ID/URL
or pasted text), it creates the feature branch and a distilled
`docs/features/{ticket-id}-{slug}/00-task.md` before doing any code
investigation. This is mechanical, not a separate approval-gated phase — it
still ends with the same human review/approval as any other Research run
before Design can start.

Rules that apply to every phase:

1. **Never move forward until the current phase is completed and approved by a human.**
   Ambiguity discovered in a later phase means "go back one phase" — never "guess and continue."
2. **Small context wins.** Each phase gets the minimum input it needs, nothing more.
3. **One current version only.** Never keep `plan-v2.md` / `plan-final.md`. Edit files
   in place. Use `git log` / `git blame` / `git diff` for history.
4. **Review is continuous**, not a single final step — use the specialised
   `agents/review-*.md` personas after every implementation phase, not one
   "review everything" pass at the end.

## Folder map

| Folder | Answers | Contents |
|---|---|---|
| [`agents/`](./agents) | **WHO** | Role/persona definitions per phase — identity, constraints, what the role must/must-not do. Generic, no project specifics. |
| [`commands/`](./commands) | **HOW TO INVOKE** | The runnable entrypoint prompt for each phase — wires an agent + relevant skills + expected output format into one command a developer/agent runs. |
| [`skills/`](./skills) | **HOW TO DO X** | Reusable procedures loaded on demand, not tied to one phase (e.g. generate tests, write an OpenAPI spec, run a DB migration). |
| [`context/`](./context) | **WHAT IS THIS PROJECT** | Project-specific payload: tech stack decisions, domain glossary, module map, known pitfalls. This is what makes the generic agents/commands/skills work for *this* repo. |
| [`standards/`](./standards) | **WHAT IS "GOOD" HERE** | Code style, architecture, testing, API, database, Kafka, and documentation standards. Always-on — every phase must comply. |

## How a feature actually flows

1. Run `commands/research.md <ticket-id-or-pasted-text>` — it creates the
   branch `feature/<ticket-id>-<slug>`, writes
   `docs/features/<ticket-id>-<slug>/00-task.md`, researches the codebase,
   proposes any `context/` updates, and produces `01-research.md`.
2. Human reviews `01-research.md` (and proposed context updates) → approve.
3. Run `commands/design.md` → human reviews `02-design/` → approve.
4. Run `commands/plan.md` → human reviews `03-plan/` → approve.
5. Run `commands/implement.md`, one plan-phase at a time (10–20 min chunks each),
   with `agents/review-*.md` gates after every phase.
6. Final cross-phase review, smoke test, commit (see `commands/implement.md`).

## Standards are law

Everything in `standards/` is a hard rule, not a suggestion. Code, design
diagrams, and plans that violate `standards/` get rejected in review — see
`agents/review-architecture.md` and `agents/review-security.md`.

## Keeping this folder healthy

- `agents/`, `commands/`, and `skills/` should stay **generic** — no domain
  entity names, no ticket numbers. They should work for any feature in this repo
  (and ideally be portable to the next Java/Spring/Postgres/Kafka project).
- `context/` is where project specifics belong. If you catch yourself adding a
  domain entity name or a legacy quirk into `agents/` or `commands/`, move it to
  `context/` instead.
- Changes to this folder go through a normal PR — no direct pushes to `main`.
