---
name: implement
description: Execute an approved plan phase by phase with 4 independent quality gates (build, architecture, security, completeness) after every phase.
argument-hint: [feature-slug]
---

# Implement Command

Uses personas: `ai/agents/implementation.md` (implementer),
`ai/agents/review-build.md`, `ai/agents/review-architecture.md`,
`ai/agents/review-security.md`, `ai/agents/review-completeness.md` (reviewers).

> **Core principle:** No phase is complete until all 4 quality gates pass. No exceptions.

## Phase 0: Understand the mission

Read the entire `03-plan/README.md` and every `phase-NN.md`, skipping phases
already checked off. Read the linked design docs for architectural context.

## Phase 1: Set up reviewers

If your agent tool supports **parallel/persistent sub-agents** (e.g.
background tasks, a multi-agent "team" API): spawn one implementer and four
persistent reviewers once, reuse them for every phase via messages.

If it does not: run the four review passes **sequentially**, using the same
four prompts (`ai/agents/review-*.md`) back-to-back after every phase — the
outcome must be identical, only the mechanics differ.

## Phase 2: Execute, phase by phase

```
IMPLEMENTER:  read phase-NN.md → implement → self-check (compile/test/verify) → report done
      ↓
REVIEWS:      run review-build, review-architecture, review-security,
              review-completeness against the SAME phase (in parallel or in sequence)
      ↓
   ┌──────────────┴──────────────┐
   ▼                             ▼
ANY REVIEW FAILED             ALL 4 PASSED
   │                             │
   ▼                             ▼
combine ALL findings from      mark phase done,
ALL failed reviews into one    move to next phase
message back to the implementer
```

When rejecting, group findings by reviewer so the implementer can address
each systematically:

```markdown
## Phase [N] REJECTED
### Build/Test/Verify
[findings]
### Architecture + Standards
[findings]
### Security
[findings]
### Completeness
[findings]

Fix all findings and re-report.
```

### Handling plan/reality mismatches

- **Minor** (line numbers, file renamed) — decide and instruct the implementer directly.
- **Architectural** (different pattern needed, missing module) — STOP, ask the human:

```markdown
## Issue in Phase [N]: [Name]
**Expected (plan):** ...
**Found (actual):** ...
**Why it matters:** ...
**Proposed solution:** ...
How should we proceed?
```

## Phase 3: Final cross-phase review

After all phases pass individually, run all four reviews again with
**cross-phase scope**:

- Build/Test/Verify — full project build/test/verify.
- Architecture — read ALL new files across ALL phases; unique `MessageKeys`,
  no orphaned code, naming consistency, DI chain correctness.
- Security — full audit across all new files.
- Completeness — read the FULL plan; verify every phase, every acceptance
  criterion, every success criterion from `03-plan/README.md`.

## Phase 4: Smoke test

```bash
docker compose -f local/docker/docker-compose.yml up -d   # Postgres, Kafka
./mvnw spring-boot:run -Dspring-boot.run.profiles=local &
sleep 10

curl -s http://localhost:8080/actuator/health | jq .
curl -s -X POST http://localhost:8080/api/{path} \
  -H "Content-Type: application/json" -H "Authorization: Bearer {token}" \
  -d '{"field": "value"}' | jq .

kill %1
docker compose -f local/docker/docker-compose.yml down
```

Verify: correct HTTP status codes, response shape matches
`08-api-contract.md`, errors come from the global exception handler (no raw
stack traces), auth is enforced (401/403), no `ERROR`-level surprises in logs.
Do NOT proceed with a failing smoke test — fix and re-run.

## Phase 5: Handoff summary

```markdown
## Implementation Complete: {Feature Name}
### Phases
- ✅ Phase 1: [summary]
### Files Changed
### Quality Gates
| Phase | Build | Arch | Security | Completeness | Verdict | Rejections |
### Final Review
### Smoke Test
### Notes — plan deviations, all acceptance criteria met (yes/no)
```

## Phase 6: Commit

- Conventional Commits format: `feat`, `fix`, `refactor`, `test`, `chore`.
- Scope in parentheses for the module: `feat(orders): add cancellation flow`.
- Stage only the files changed by this feature.
- Do NOT push automatically — report the local commit hash and let the human push.

## Phase 7: Save manual QA flow

Generate `docs/features/{feature-slug}/manual-qa.md`: prerequisites, one
scenario per happy path / validation error / auth check, with exact curl
commands and expected responses — written for a human tester who doesn't know
the codebase.

## Rules

1. No phase without all 4 gates passing — no shortcuts.
2. `ai/standards/` violations are automatic rejects, not "notes."
3. Full file reads only — partial reads cause partial understanding and bugs.
4. Stop at mismatches — ask the human, never improvise.
5. Track rejections — recurring rejection patterns mean `ai/standards/` needs updating.
6. Plan is law — implementation must match it exactly. Scope reduction = reject.
   Only improvements that ADD to the plan (without contradicting it) are acceptable.
