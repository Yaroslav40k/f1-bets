# Agent Instructions

Before doing anything in this repository, read **`ai/README.md`**. It is the
single source of truth for how AI agents work here: a 4-phase methodology
(Research → Design → Planning → Implementation), with a human approval gate
between every phase.

## Non-negotiable rules

1. Never write implementation code without an approved Plan
   (`ai/commands/plan.md` output, reviewed by a human).
2. Never start Planning without an approved Design; never start Design
   without approved Research.
3. All code must comply with `ai/standards/` — treat it as a hard constraint,
   not a suggestion.
4. Ticket-specific / domain-specific facts belong in `ai/context/` (a fixed
   set of files — see `ai/context/README.md`) or in
   `docs/features/<ticket-id>-<slug>/` — never invent new top-level folders
   for this.

## How to run a phase

Each phase has two files:
- `ai/agents/<phase>.md` — the persona/identity for that phase.
- `ai/commands/<phase>.md` — the actual runnable procedure. **This is what
  you follow when a developer asks you to run a phase.**

Available phases, in order: `research`, `design`, `plan`, `implement`.
Also available on demand: `ai/commands/review.md` and the specialised
`ai/agents/review-*.md` personas.

`research` also handles turning a raw Jira ticket (ID/URL or pasted text)
into a branch and a distilled `docs/features/<ticket-id>-<slug>/00-task.md`
before investigating the codebase — there is no separate intake step.

When a developer says something like "run research on PROJ-1234" or "do
research on X", open the corresponding `ai/commands/<phase>.md` file and
follow it literally — it already wires together the right persona, skills,
and expected output format/location.
