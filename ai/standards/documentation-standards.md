# Documentation Standards

## Code comments

- **Prefer self-documenting code over comments.** A comment explaining *what*
  a block of code does is usually a sign the code should be extracted into a
  well-named method instead.
- Comments that explain *why* (a non-obvious business rule, a workaround for
  a specific known issue, a trade-off) are welcome — reference
  `context/known-pitfalls.md` or an ADR (`03-decisions.md`) if applicable.
- No commented-out code — delete it; Git history is the archive.
- No `TODO`/`FIXME` without a linked ticket. Unlinked TODOs are rejected in review.

## Javadoc

Enforced by Checkstyle (`standards/checkstyle/checkstyle.xml`):

- Required on all `public` classes/interfaces and `public` methods in
  packages exposed as module boundaries (facades, public DTOs) — internal
  `service`/`repository` implementation classes don't need it if the method
  name and types are self-explanatory.
- First sentence is a complete summary sentence, ending with a period
  (`SummaryJavadoc`). Single-line Javadoc is allowed only for trivial
  one-line summaries (`SingleLineJavadoc`).
- `@param`, `@return`, `@throws` tags in that order (`AtclauseOrder`), each
  with a non-empty description (`NonEmptyAtclauseDescription`).
- No blank Javadoc paragraphs; consistent asterisk alignment
  (`JavadocParagraph`, `JavadocMissingWhitespaceAfterAsterisk`).
- `@deprecated` tag requires a corresponding `@Deprecated` annotation and
  vice versa (`MissingDeprecated`) — always explain the replacement/migration
  path in the tag text.

## READMEs and design docs

- Every feature's `docs/features/{slug}/` folder is the durable documentation
  for that feature — not a Confluence page, not a Slack thread. Code review
  should treat missing/stale design docs as a blocker, same as missing tests.
- `ai/context/*.md` files are living documentation of the project itself —
  update them as part of the PR that changes the reality they describe, not
  as a follow-up "documentation task."

## API documentation

Generated from annotated controllers via springdoc-openapi — see
`skills/write-openapi-spec.md`. Do not hand-maintain a separate Swagger/OpenAPI
YAML file that can drift from the code.

## One current version only

Never keep multiple versions of the same document
(`design-v2.md`, `plan-final.md`, `README-old.md`). Edit in place; rely on
`git log` / `git blame` / `git diff` for history. This applies to every
document under `ai/` and `docs/features/`, without exception.
