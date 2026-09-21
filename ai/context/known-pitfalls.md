# Known Pitfalls

> Template — this file grows over time. Every time a bug, incident, or
> AI-generated regression traces back to a "known trap," add an entry here so
> Research/Design/Implementation agents stop rediscovering it the hard way.
> A generic agent has no memory of this project's history — this file is that
> memory.

## How to add an entry

1. One entry per trap. Be specific, include a file reference if applicable.
2. State the trap, why it's tempting, and the correct approach.
3. Link to the incident/PR/postmortem if one exists.

## Entries

### [Trap name]
- **Where:** `path/to/file.java` or module name
- **The trap:** [what looks correct but isn't]
- **Why it's tempting:** [why an agent or a new dev would naturally do this]
- **Correct approach:** [what to do instead]
- **Reference:** [incident/PR link, if any]

<!-- Repeat per pitfall. Delete this template block once real entries exist. -->

## Categories to watch for (fill in as discovered)

- Race conditions around background jobs / async Kafka consumers.
- Fragile auth/session flows.
- Stale-cache assumptions (Caffeine/Redis, if introduced later).
- Silent data-loss patterns in migrations (e.g. dropping a column too early
  in a rolling deploy — see `standards/database-standards.md`).
- Any place where an AI agent previously produced plausible-but-wrong code
  because the context was incomplete — document what was missing so the next
  agent gets the full picture.
