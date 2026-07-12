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
