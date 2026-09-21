---
name: plan
description: Turn an approved design into a phased implementation plan (10-20 min phases). Human approval required before implementation starts.
argument-hint: [feature-slug]
---

# Plan Command

Uses persona: `ai/agents/planning.md`.

## 1. Read everything

Read `docs/features/{feature-slug}/01-research.md` and the entire
`docs/features/{feature-slug}/02-design/` folder, plus all of `ai/standards/`.

## 2. Choose a phase-ordering strategy

Pick Bottom-up / Adapter-first / Vertical slice (see `ai/agents/planning.md`)
and document the choice and why in `03-plan/README.md`.

## 3. Output structure

```
docs/features/{feature-slug}/03-plan/
  README.md     — overview, phase table, file map, DI notes, error codes, success criteria
  phase-01.md
  phase-02.md
  phase-NN.md   — one file per phase, each self-contained
```

### `README.md` template

```markdown
---
date: YYYY-MM-DD
feature: {feature-slug}
design: ../02-design/README.md
status: draft | approved
---

# Code Plan: {Feature Name}

## Overview
## Phase Strategy
## Phases
| Phase | Layer | Depends on | Status |
## File Map
### New Files
### Modified Files
## DI Integration
## Error Codes
| Code | Description | HTTP Status |
## Success Criteria
- [ ] All phases completed and reviewed
- [ ] All tests passing
- [ ] `./mvnw verify` clean
- [ ] API contract matches implementation
- [ ] All acceptance criteria from design README met
```

### `phase-NN.md` template

```markdown
---
phase: N
name: {Phase Name}
layer: domain | service | repository | controller | kafka | config
depends_on: [phase-01] or none
---

# Phase {N}: {Phase Name}

## Goal
## Context
[what earlier phases produced that this phase builds on]

## Files to Create
### `path/to/File.java`
**Purpose:**
**Implementation details:** [business rules, invariants, reference to design docs — never actual code]

## Files to Modify
### `path/to/Existing.java`
**What changes:**

## Key Decisions
[reference 03-decisions.md if relevant]

## Verification
- [ ] `./mvnw -q clean compile`
- [ ] `./mvnw -q test` for the touched module
- [ ] [phase-specific checks]
```

## 4. Human approval

Present phase strategy, phase list, file scope, and error code range.
**WAIT for explicit approval** before running `commands/implement.md`.
