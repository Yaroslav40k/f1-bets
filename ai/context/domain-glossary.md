# Domain Glossary

> Template — populate as the domain model is designed. Every entity name used
> in code, tests, and design docs must match this glossary exactly. If code
> and glossary disagree, the glossary is wrong and must be fixed immediately
> (never let two "sources of truth" coexist).

## How to use this file

- One entry per domain entity/aggregate/value object.
- Research/Design/Planning/Implementation agents should treat this as the
  canonical vocabulary — do not invent synonyms for the same concept.
- Mark fields containing sensitive data explicitly — the security reviewer
  (`agents/review-security.md`) checks this list.

## Entities

### `{EntityName}`
- **Meaning:** [what this represents in the business domain]
- **Key fields:** [field name → type → constraints/invariants]
- **Sensitive fields:** [none, or list — PII, credentials, financial data]
- **Lifecycle / states:** [state machine if applicable]
- **Owning module:** [see `module-map.md`]
- **Related events:** [Kafka events this entity emits/consumes, if any]

<!-- Repeat one block per entity. Delete this template block once real
     entities are documented. -->

## Terms & abbreviations

| Term | Meaning |
|---|---|
| [ACRONYM] | [full meaning, in this product's specific context] |

## Error code ranges

Track which prefix/range each module owns so Design never assigns a
colliding error code.

| Module | Prefix | Range in use | Next available |
|---|---|---|---|
| [module] | [PREFIX] | [XXX–YYY] | [ZZZ+] |
