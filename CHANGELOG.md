# Changelog

All notable changes to this project are documented in this file.

## [Unreleased]

### Added

- Emit a versioned `html4tree/1` generator marker and refuse to replace
  user-authored, malformed, late-marker, symlink, or directory `index.html`
  files. `--cleanup` / `--dry-run` delete or report only owned artifacts.
- Emit a `noindex, nofollow` robots meta preference on every generated
  directory page, with the explicit boundary that supporting crawlers must
  first fetch the page and that confidential data still requires server-side
  protection.

### Changed

- Require `--cleanup` whenever `--dry-run` is supplied, bound generated-index
  ownership inspection to the configured prefix instead of reading the whole
  file, and retain/report the recovery backup when both publication and
  automatic restoration fail.
- Publish first-time `index.html` files with an exclusive hard link
  (`Files.createLink` / POSIX `link(2)`), falling back to a create-only
  move (no `ATOMIC_MOVE`, no `REPLACE_EXISTING`) when hard links are
  unavailable. Reclassify immediately before cleanup delete and before
  failure restore so a late customer page is not replaced or removed.
- Remove the README `find ... -name index.html -delete` cleanup command. That
  command cannot distinguish generated pages from customer home pages.
- Improve generated directory-index readability with adjacent-row separators,
  explicit light and dark empty-state text colors, and text-only hover/focus
  underlining while retaining the full interactive target's focus outline.

### Fixed

- Generate the inline-style Content Security Policy SHA-256 source expression
  from the exact normalized UTF-8 stylesheet bytes emitted into each generated
  `index.html` file, preventing template whitespace from invalidating the policy.

### Tests

- Add ownership regressions for user-authored preservation, owned replacement,
  cleanup/dry-run selection, CLI misuse rejection, bounded prefix EOF handling,
  opened-stream read failure, vanished-backup publication failure, atomic-conflict
  refusal, backup restore, retained-backup reporting, exclusive-create
  refusal of a real occupant, hard-link fallback, late-occupant
  reclassification, restore reclassification, and cleanup revalidation
  before delete.
- Add a real generated-file regression test that independently recomputes the
  declared style hash from the emitted `<style>` text.
- Add generated-page regressions for row ordering, empty-state semantics, CSS
  cascade ordering, reduced-motion retention, text-only decoration, and numeric
  text/focus contrast thresholds.

### Documentation

- Record the generated-index ownership, overwrite, cleanup, and migration
  contract in `docs/doctoring/generated-index-ownership.md`.
- Record the generated-page robots indexing preference, crawler-access
  prerequisite, non-security boundary, rollback contract, and current Google
  Search Central reference in `docs/doctoring/robots-indexing-preference.md`.
- Record the CSP byte-identity decision, threat boundary, verification contract,
  and current W3C Working Draft reference in `docs/doctoring`.
- Record the generated-index readability decision, WCAG 2.2 engineering basis,
  contrast calculations, scope boundaries, and verification contract.
