---
name: research
description: Turn a raw ticket (Jira ID/URL, or pasted text) into a branch and a distilled task file, then research the codebase and produce docs/features/{slug}/01-research.md. Facts only — no design decisions.
argument-hint: [ticket-id-or-url-or-pasted-text-or-feature-slug] [description]
---

# Research Command

Uses persona: `ai/agents/research.md`. Uses skill: `ai/skills/sync-task-from-jira.md`
(only for the ticket-intake sub-step below).

## 1. Parse arguments

`$ARGUMENTS` is one of:
- A ticket ID (`PROJ-1234`) or ticket URL → fetch via Jira MCP if configured.
- Raw pasted ticket text → use directly, no fetch needed.
- An existing feature slug + a free-form research question (for follow-up
  research on a feature that already has a `docs/features/{slug}/` folder).

If none of these can be identified, ask the user to provide one.

## 2. Ticket intake (skip if arguments were already a slug + question)

1. Derive a short kebab-case slug from the ticket title (e.g.
   `order-cancellation`).
2. Create the branch: `feature/{ticket-id}-{slug}`.
3. Write `docs/features/{ticket-id}-{slug}/00-task.md`:

   ```markdown
   ---
   ticket: {ticket-id}
   jira: {ticket-url}
   epic: {epic-id-or-link, if any}
   confluence: {linked confluence page(s), if any}
   ---
   ## Business goal
   [2-3 sentences — why this exists, not the full ticket description]

   ## Acceptance criteria
   [bulleted, copied verbatim from the ticket]

   ## Constraints / non-goals
   [anything explicitly out of scope, if stated]
   ```

   See `ai/skills/sync-task-from-jira.md` for exactly what to keep vs. drop
   when distilling the raw ticket text.

## 3. Read context first

Before researching, read all four `ai/context/*.md` files
(`tech-stack.md`, `domain-glossary.md`, `module-map.md`,
`known-pitfalls.md`). This is cheap (four short files) and grounds the
research in what's already known — don't rediscover facts that are already
documented.

## 4. Decide if deep research is needed at all

Skip straight to a short research doc (referencing `context/module-map.md`
directly) only if the affected area is already fully documented there and no
unfamiliar code is touched. Otherwise, full research is required.

## 5. Decompose and spawn

1. Read any directly mentioned files COMPLETELY (no limit/offset).
2. Decompose the question into 2–4 independent investigation areas
   (e.g. "domain model", "REST layer", "Kafka integration", "existing similar feature").
3. If your tool supports parallel sub-agents, spawn 2–4 of them using the
   `ai/agents/research.md` persona — never more than 4 (context overflow risk).
   Otherwise, run the investigations sequentially, one at a time.
4. Each task prompt MUST include: the specific question, starting
   files/packages if known, the required output format (see persona), and
   explicit scope boundaries (what NOT to investigate).

## 6. Synthesize

Merge findings, resolve contradictions, cross-reference. If gaps remain,
spawn at most one follow-up round.

## 7. Cross-check context for gaps

While synthesizing, cross-check every domain term/entity/module the ticket
or the code touches against `ai/context/domain-glossary.md` and
`ai/context/module-map.md`. For anything missing or contradicted by what you
found in the code, prepare a proposed diff — do not apply it yet, and never
create a new file under `ai/context/` (it's a fixed set — see
`ai/context/README.md`).

## 8. Save

```
docs/features/{feature-slug}/01-research.md
```

```markdown
---
date: YYYY-MM-DD
feature: {feature-slug}
researcher: {agent/tool name}
commit: {git rev-parse --short HEAD}
branch: {git branch --show-current}
question: "{original research question or ticket id}"
---

# Research: {Feature Name}

## Summary
[2-3 paragraphs]

## Findings
[per ai/agents/research.md output format]

## Code References
[file:line - description]

## Open Questions
[anything unresolved]

## Proposed context updates
[diffs against ai/context/domain-glossary.md / module-map.md, or "none"]
```

## 9. Hand off

Present the summary **and** the proposed context updates to the human in
the same message, and stop — do not run `commands/design.md` yet.

This step is synchronous and happens entirely within the Research
conversation, in this order:

1. Human replies with approval (of the research doc, the context diffs, or
   both — they may approve/reject each independently, e.g. "approved,
   apply diff 1, skip diff 2").
2. **Before ending this turn**, the agent applies only the approved diffs
   directly to the existing `ai/context/*.md` files (never a new file).
   Diffs not approved are dropped, not left "pending" anywhere.
3. Research is now complete. `context/` already reflects the approved
   changes by the time `commands/design.md` is run later — Design does not
   read or apply any pending diffs itself; it just sees current files.

If the human flags a gap in the research itself (not the context diffs),
re-run research on just that gap — do not redo everything.
