# Context — Index

This folder holds **project-specific** information: the payload that makes
the generic `agents/`, `commands/`, and `skills/` actually work for *this*
repository. Nothing generic belongs here; nothing project-specific belongs
in `agents/`, `commands/`, or `skills/`.

| File | Purpose |
|---|---|
| [`tech-stack.md`](./tech-stack.md) | Concrete stack decisions: Java/Spring/Postgres/Kafka versions and key libraries. |
| [`domain-glossary.md`](./domain-glossary.md) | Domain entities and terms, in their exact meaning for this product. |
| [`module-map.md`](./module-map.md) | Package/module layout and ownership. |
| [`known-pitfalls.md`](./known-pitfalls.md) | Legacy traps, known race conditions, fragile flows — so agents don't repeat known mistakes. |

## This is a closed set of files

`context/` is **not** a dumping ground. The four files above are the entire
schema — do not add a fifth file "just for this ticket" or "just for this
epic." Ticket-specific and epic-specific material belongs in
`docs/features/{ticket-id}-{slug}/`, never here.

- `agents/research.md` may only **propose edits to
  these existing files** (an addition or correction inside
  `domain-glossary.md` or `module-map.md`), never create a new file.
- If something genuinely doesn't fit any of the four files, that is a
  deliberate human decision (e.g. during a Design review), not something an
  agent decides mid-Research.
- Enrichment is **just-in-time**: don't front-load Confluence into these
  files "to be safe." Add only what a specific ticket's Research/Design
  actually surfaced as missing or wrong.

## Maintenance rule

Update these files as the project evolves — they are living documents, edited
via normal PRs like everything else in `ai/`. Stale context is worse than no
context: it actively misleads agents (see `standards/` for how "noise"
degrades AI output quality).
