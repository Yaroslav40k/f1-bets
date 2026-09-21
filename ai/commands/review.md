---
name: review
description: Run the four specialised review passes (build, architecture, security, completeness) against a given diff/branch outside the normal implement loop — e.g. for an ad-hoc PR review.
argument-hint: [base-branch] [feature-slug (optional, for design/plan compliance)]
---

# Review Command

Uses personas: `ai/agents/review-build.md`, `ai/agents/review-architecture.md`,
`ai/agents/review-security.md`, and (only if `feature-slug` is given)
`ai/agents/review-completeness.md`.

## Why four separate passes, not one

A single "review everything" prompt performs poorly: it produces vague,
partial coverage and a false sense of safety. Four narrow, specialised passes
each catch what they're built to catch.

## 1. Collect the diff

```bash
git diff {base-branch}...HEAD --name-only
```

## 2. Run each reviewer against the same file list

Run in parallel if your tool supports it, otherwise sequentially:

1. `agents/review-build.md` — `./mvnw -q clean compile && ./mvnw -q test && ./mvnw -q verify`
2. `agents/review-architecture.md` — reads every changed file against `ai/standards/`
3. `agents/review-security.md` — reads every changed file for vulnerabilities
4. `agents/review-completeness.md` — only if `feature-slug` is provided and
   `docs/features/{feature-slug}/03-plan/` exists

## 3. Aggregate

```markdown
## Review Summary — {base-branch}...HEAD

| Reviewer | Verdict |
|---|---|
| Build/Test/Verify | ✅ / ❌ |
| Architecture + Standards | ✅ / ❌ |
| Security | ✅ / ❌ |
| Completeness (if applicable) | ✅ / ❌ |

[Combined findings, grouped by reviewer]

**Overall: APPROVED / CHANGES REQUESTED**
```

Any single ❌ → overall CHANGES REQUESTED. Present this to the human — this
command never merges or approves on its own; a human always makes the final
call.
