# Code Style

## Baseline

Google Java Style Guide is the default for anything not explicitly overridden
below. Where this document conflicts with Google Style, **this document wins**
— these are the project's deliberate deviations, enforced by
`standards/checkstyle/checkstyle.xml` (run via `./mvnw verify`) and the
IntelliJ scheme at `standards/checkstyle/intellij-code-style.xml` (import via
*Settings → Editor → Code Style → Java → Import Scheme*).

## Formatting

- Indentation: 4 spaces, no tabs. XML/YAML: 2 spaces.
- Line length: 120 characters max (`LineLength`; URLs in comments are exempt).
- File length: 1000 lines max (`FileLength`) — beyond that, split the class.
- Braces are mandatory on every `if`/`for`/`while`/`do` — no single-statement
  bodies without braces (`NeedBraces`).
- `} else {` / `} catch {` / `} finally {` on the same line as the closing
  brace (`RightCurly`); standalone closing braces for method/class bodies.
- One statement per line (`OneStatementPerLine`); no multiple variable
  declarations on one line (`MultipleVariableDeclarations`).
- No wildcard imports; imports are never collapsed to `*` regardless of count.

## Import order

Static imports grouped separately from regular imports, both ordered:
`java.*` → `javax.*` → `com.andersen.*` → everything else, alphabetically
within each group, with a blank line between groups (`ImportOrder`). No unused
imports (`UnusedImports`), no redundant imports (`RedundantImport`).

`import-control.xml` restricts what may be imported inside `com.andersen`
packages — see `standards/architecture-standards.md` for the layering rules
this enforces (e.g. controllers must not import repositories directly).

## Naming

- Classes/interfaces: `UpperCamelCase`. One top-level class per file
  (`OneTopLevelClass`), filename matches the class name (`OuterTypeFilename`).
- Methods/fields/local variables/parameters: `lowerCamelCase`.
- Constants (`static final`): `UPPER_SNAKE_CASE`.
- Type parameters: single uppercase letter, optionally followed by a digit
  (`ClassTypeParameterName`, `MethodTypeParameterName`, `InterfaceTypeParameterName`).
- Avoid consecutive-capital abbreviations in names beyond a reasonable limit —
  prefer `HttpClient` over `HTTPClient` (`AbbreviationAsWordInName`).
- Lambda parameters follow the same `lowerCamelCase` rule as method parameters
  (`LambdaParameterName`).

## Language rules

- **No magic numbers** in business logic (`MagicNumber`) — extract a named
  constant. Suppressed in test code (see `standards/checkstyle/suppressions.xml`),
  but prefer named constants there too when the number's meaning isn't obvious.
- **`this.` required** to disambiguate instance member access when a local
  variable/parameter shadows it (`RequireThis`) — don't rely on implicit resolution.
- No unnecessary parentheses (`UnnecessaryParentheses`), no unnecessary
  semicolons anywhere (`UnnecessarySemicolon*`).
- No empty catch/finally/if blocks without an explanatory comment (`EmptyBlock`).
- No `clone()` overrides (`NoClone`), no finalizers (`NoFinalizer`) — use
  `try-with-resources` / `AutoCloseable` instead.
- Declare a variable as close as possible to first use
  (`VariableDeclarationUsageDistance`).
- Overloaded methods must be declared next to each other
  (`OverloadMethodsDeclarationOrder`).
- Members ordered: static fields → instance fields → constructors → methods
  → inner types last (`DeclarationOrder`, `InnerTypeLast`).
- Maximum 4 `throws` declarations per method (`ThrowsCount`) — if you need
  more, your method is doing too much or your exception hierarchy is wrong.
- Maximum 40 methods per class total (`MethodCount`) — a signal to split
  the class, not to suppress the check.
- Nested `if` depth ≤ 3, nested `for` depth ≤ 2 (`NestedIfDepth`, `NestedForDepth`).
- Boolean expression complexity ≤ 4 operators (`BooleanExpressionComplexity`)
  — extract named boolean methods for anything more complex.
- All class fields are `private`; expose via getters/methods, never public
  fields (`VisibilityModifier`) — except `public static final` constants.

## Lombok conventions

- Simple entities/DTOs: `@Data @Builder @NoArgsConstructor @AllArgsConstructor`.
- Complex JPA entities with bidirectional relations:
  `@Getter @Setter @NoArgsConstructor @ToString(exclude = "...") @EqualsAndHashCode(exclude = "...")`
  — always exclude bidirectional relation fields from `toString`/`equals`/`hashCode`
  to avoid infinite recursion.
- Services/configuration classes: `@RequiredArgsConstructor @Slf4j` — constructor
  injection only, never field injection (`@Autowired` on fields is rejected in review).

## Comments and Javadoc

See `standards/documentation-standards.md`.

## Enforcement

- `./mvnw verify` runs Checkstyle as part of the build — a violation fails CI.
- `standards/checkstyle/suppressions.xml` documents the only accepted
  exceptions (primarily relaxed rules inside `src/test`). Do not add new
  suppressions without a documented reason and team agreement.
