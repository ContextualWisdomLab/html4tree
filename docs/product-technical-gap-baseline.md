# Product–Technical Gap Baseline

This document records code-current product and technical gaps for `html4tree`. Live source, tests, the protected branch, and the active pull request remain authoritative when they differ from prose.

## Current security boundary

`html4tree` treats `.html4ignore` as directory-local policy input. The policy is optional, but when present it must not be followed through a symbolic link and must not be parsed after an independent pathname validation step that can become stale before use.

The reader opens the policy through `Files.newByteChannel(..., READ, NOFOLLOW_LINKS)`, validates the declared size, performs a bounded read, and verifies that the opened handle did not grow or shrink while being read. A missing policy is the normal no-policy case. Other open/read failures are mapped to `IgnoreFileReadException`; `crawl_directories` then skips that directory rather than widening the failure to the entire crawl.

The 1 MiB policy-file limit and 1,000-line/100-character-pattern limits are product-local resource bounds. They are not presented as thresholds prescribed by CWE or the JDK. Oversized policy input fails closed.

## Repaired gap: partial directory snapshots

### Problem

The caller may provide a partial `dirFilesNames` snapshot for matching. The previous implementation also used membership in that snapshot to decide whether `.html4ignore` existed. If the policy file existed on disk but was omitted from the caller snapshot, policy admission was skipped and ignored entries could become visible.

### Constraints

- Do not restore a pathname `exists`/`isFile`/`isSymbolicLink` precheck followed by a second open.
- Preserve one-handle `NOFOLLOW_LINKS` read semantics.
- Preserve cause-bearing domain exceptions for directory, symlink, oversize, read failure, growth, and shrink cases.
- Treat absence as no policy without turning absence into a crawl failure.
- A caller snapshot limits which directory entries are matched; it does not authorize whether policy exists.

### Decision

Attempt the no-follow policy open independently of snapshot membership. `NoSuchFileException` means no policy. Any other I/O failure remains fail-closed. Apply the parsed matchers only to the caller snapshot (or the directory listing when no snapshot was supplied).

The rejected alternative was a pre-open existence or symlink check. That would recreate a time-of-check/time-of-use window: MITRE CWE-367 describes the weakness as checking a resource state and later using the resource after that state may have changed. Oracle's `LinkOption.NOFOLLOW_LINKS` contract is used at the open boundary to avoid following symbolic links.

### TDD evidence

Causal source head `ff5c180ec6b89b05e3b5ccd8ecbdb4212d1770a1` removes snapshot-membership admission and makes missing-policy handling explicit at the no-follow open boundary. Test descendant `c35fdc060e4422103414e4133f915f505c6de8e4` updates the stale oversized-policy regression to require `IgnoreFileReadException`. Existing tests also cover partial snapshots, malformed directory names, symlink/directory policies, read-stage I/O errors, handle growth/shrink, and symlink replacement at open.

Hosted CI/security results for a later documentation descendant are merge authority only when they are bound to that exact descendant. Predecessor results are lineage evidence, not transferable GREEN.

## Remaining buyer-visible gaps

The current patch is a security-correctness repair, not a complete commercial release. Remaining gaps include exact-head application/security GREEN, independent current-head review, reproducible release/SBOM/provenance evidence, rollback verification, and measured performance on representative real directory trees. Existing optimization comments are not treated as performance evidence without a protected comparator and repeated measurements.

The generated directory UI also still requires current-head rendered browser evidence before material UI can be called complete: keyboard focus/hover, narrow and intermediate widths, long filenames, empty state, light/dark modes, reduced motion, and accessibility-tree behavior. Localization beyond the current Korean output is not claimed.

## Acceptance

A release candidate must preserve the policy contract above and show on one exact protected generation: application tests passing; security/SAST/CodeQL terminal-valid; no actionable current-head review thread; release artifact, SBOM and provenance bound to the release; reproducibility and rollback evidence; and no predecessor-status substitution or gate weakening.

## References

MITRE. (2026). *CWE-367: Time-of-check time-of-use (TOCTOU) race condition*. Common Weakness Enumeration. https://cwe.mitre.org/data/definitions/367.html

MITRE. (2026). *CWE-400: Uncontrolled resource consumption*. Common Weakness Enumeration. https://cwe.mitre.org/data/definitions/400.html

Oracle. (2026). *LinkOption (Java SE 26 & JDK 26)*. Java Platform API Specification. https://docs.oracle.com/en/java/javase/26/docs/api/java.base/java/nio/file/LinkOption.html
