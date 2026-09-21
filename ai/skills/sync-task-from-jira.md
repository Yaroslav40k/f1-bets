---
name: sync-task-from-jira
description: Fetch a Jira ticket (via MCP or pasted text), distill it into a task file, and cross-check ai/context/ for gaps. Used by the ticket-intake sub-step of commands/research.md.
---

# Skill: Sync Task From Jira

## When to use

Every time `commands/research.md` starts from a raw ticket (rather than an
existing feature slug) — this is the procedure it follows for that
sub-step, before any code investigation begins.

## 1. Obtain the ticket content

- **If a Jira MCP server is configured:** fetch the ticket by ID/URL —
  title, description, acceptance criteria field (if the team uses a
  dedicated field/template), epic link, and any linked Confluence pages.
- **If no MCP is configured:** use the pasted ticket text the developer
  provided. Do not attempt to guess or fabricate fields that weren't given.
- Either way, this is a **read-only fetch** — never write back to Jira/Confluence
  from this skill.

## 2. Distill — what to keep, what to drop

| Keep | Drop |
|---|---|
| Title | Comment threads |
| Business goal (rewritten as 2-3 sentences, not the raw description) | Internal Jira workflow metadata (status, sprint, story points) |
| Acceptance criteria (copied **verbatim**) | Attachments (link to them instead of embedding) |
| Explicit constraints/non-goals if stated | Speculative discussion ("what if we also...") — not yet in scope |
| Epic link, Confluence link(s) | Anything not directly about this ticket's scope |

Rewriting the business goal (rather than pasting the raw description) is
deliberate: raw ticket descriptions are often written for humans with tribal
context and contain noise (links, "as discussed with X", historical edits).
Acceptance criteria are the one section copied exactly, because rephrasing
them risks silently changing the contract.

## 3. Derive the slug and write the task file

- Slug: lowercase, kebab-case, 2-4 words from the title
  (`"Add order cancellation with refund"` → `order-cancellation-refund`).
- Write to `docs/features/{ticket-id}-{slug}/00-task.md` using the template
  in `ai/commands/research.md`.

## 4. Context gap cross-check

1. Extract candidate domain terms from the ticket (entity names, module
   names, business terms — usually capitalized nouns or terms repeated
   across the acceptance criteria).
2. Grep each candidate against `ai/context/domain-glossary.md` and
   `ai/context/module-map.md`.
3. For each term **not found**, or found but described differently than the
   ticket implies, prepare a proposed diff:
   ```markdown
   ### Proposed addition to context/domain-glossary.md
   ### `RefundPolicy`
   - **Meaning:** [inferred from ticket — mark as inferred, to be confirmed]
   - **Owning module:** [unknown — confirm with team]
   ```
4. Present all proposed diffs together. Apply only the ones the developer
   confirms. Never create a new file — only append/edit within the four
   fixed `ai/context/` files.

## 5. Output

- Git branch created.
- `docs/features/{ticket-id}-{slug}/00-task.md` written.
- Zero or more confirmed edits applied to `ai/context/domain-glossary.md`
  and/or `ai/context/module-map.md`.
- Control returns to `commands/research.md`, which continues with codebase
  investigation.
