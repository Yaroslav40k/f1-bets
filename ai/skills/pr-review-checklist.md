---
name: pr-review-checklist
description: Quick checklist a human (or the review command) runs before merging any PR, independent of the four automated review agents.
---

# Skill: PR Review Checklist

This is the human-facing complement to `agents/review-*.md` — a fast sanity
pass, not a replacement for the automated gates.

## Before requesting review

- [ ] `./mvnw -q clean verify` passes locally.
- [ ] All four review agents (`commands/review.md`) ran clean, or their
      findings are addressed/explicitly accepted with a reason.
- [ ] Branch is up to date with the target branch (rebased or merged recently).
- [ ] Commit messages follow Conventional Commits.
- [ ] No debug logging, commented-out code, or `TODO` without a linked ticket.

## While reviewing someone else's PR

- [ ] Does the diff match the linked plan phase(s)? Flag any silent scope change.
- [ ] Are new DB migrations backward-compatible with the currently deployed version?
- [ ] Are new Kafka consumers idempotent and do they have DLQ handling?
- [ ] Are new/changed error responses using `MessageKeys`, not raw strings?
- [ ] Do tests actually exercise the new behavior, or just increase coverage
      numbers without asserting anything meaningful?
- [ ] Is any secret, credential, or internal hostname exposed in code or config?
- [ ] Does the PR description explain *why*, not just *what* — reviewers
      without full context should be able to follow the reasoning.

## Merge criteria

All boxes checked, CI green, at least one human approval beyond the author.
No merging with known-failing quality gates "to save time" — see the book's
core lesson: skipping gates for speed produces a net loss within months.
