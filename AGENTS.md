# AGENTS.md

Cross-agent conventions for this repo (Claude, Codex, Cursor, opencode, and any
other coding agent). html4tree is a Kotlin CLI (Gradle build) that generates
`index.html` directory listings.

<!-- BEGIN cwl-agent-guidance -->
## Agent guidance (CWL governance)

### Security & review gate

- Every PR runs a central, required **Security Scan** gate: `osv-scan` +
  `dependency-review` (diff-scoped) and `trivy-fs` (repo-wide, CRITICAL/HIGH,
  fixable). It runs on every PR base, **including stacked PRs**.
- A failing **`trivy-fs` is a REAL finding, not a flake.** Read the job log — it
  prints each finding's rule id / severity / file — or the run's SARIF results.
  Then **remediate**:
  - Dependency vulnerability: bump the offending library in `build.gradle`
    (e.g. the Kotlin stdlib or `clikt` version, or a transitive dep).
  - Genuine false positive only: add a narrow, documented
    `.trivyignore` / `.trivyignore.yaml` entry.
  - This repo has no Dockerfile or k8s manifests today; if you add either,
    fix image/misconfig findings at the source.
- Do **NOT** weaken or disable the gate. A local `trivy` scan with a stale DB
  misses findings — run `trivy --download-db-only` first, and scan the **merge
  ref**, not just the PR head.
- The org `code_scanning` ruleset is intentionally **CodeQL-only** (multiple
  code-scanning tools can't converge on one PR ref). Gating is by the Security
  Scan **job result**, not the code_scanning rule — don't add tools to that rule.

### Code exploration

- This repo has **no `.codegraph/` index**, so use normal search (grep/find,
  Read) to locate and understand code. If a `.codegraph/` directory is later
  added at the repo root, prefer CodeGraph — `codegraph explore "<query>"` or the
  code-review-graph MCP tools — BEFORE grep/find, since it surfaces
  callers/callees/impact that text search misses.
<!-- END cwl-agent-guidance -->

## Generated listing contract

- Dynamic names (`<h1>`, `.entry-name`) use `dir="auto"` and
  `unicode-bidi: isolate`.
- Entry type text is a sibling `.visually-hidden` span so browser
  translation can reach it. Do not put type text inside the name isolate.
- `title` attributes wrap the filename with U+2068 / U+2069.
- Hover/focus underline targets `.entry-name`, never `span:last-child`.
- Neutralize bidirectional format controls in the display name (U+FFFD)
  before isolation. Do not strip U+2066–U+2069 inside `escapeHtml()`.
  Keep the real `File.name` in `href`.
- File rows show IEC size and UTC `<time datetime>` from the same
  `BasicFileAttributes` snapshot used for the symlink check. Directories
  use an em dash for size. Omit metadata when attributes cannot be read.
- Sensitive-name matching may trim/lowercase; the exclusion set stores
  the exact observed `File.name`.
- CSS colors belong in `--listing-*` tokens. `CspHashTest` must keep
  passing after any stylesheet edit.

- Generated pages emit `<meta name="generator" content="html4tree/1">`.
  Unmarked, late, malformed, symlink, and directory `index.html` targets
  are preserved. `--cleanup` deletes only owned pages.

Decision records: `docs/doctoring/bidi-isolation.md`,
`docs/doctoring/bidi-control-neutralization.md`,
`docs/doctoring/listing-entry-metadata.md`,
`docs/doctoring/generated-index-ownership.md`.

## Code-owner review gates — disabled (on hold)

As of 2026-08-04, code-owner review requirements (`require_code_owner_reviews` in branch
protection, `require_code_owner_review` in rulesets) are disabled across the ContextualWisdomLab
org: there is a single maintainer (solo developer), so a code-owner approval gate can never be
satisfied. This is ON HOLD until the org has multiple maintainers — do NOT re-enable these
settings or add CODEOWNERS-based merge gates before then.
