# Changelog

All notable changes to this project are documented in this file.

## [Unreleased]

### Added

- Emit a `noindex, nofollow` robots meta preference on every generated
  directory page, with the explicit boundary that supporting crawlers must
  first fetch the page and that confidential data still requires server-side
  protection.

### Changed

- Improve generated directory-index readability with adjacent-row separators,
  explicit light and dark empty-state text colors, and visible entry-name-only
  hover/focus underlining while retaining the full interactive target's focus
  outline.
- Isolate buyer-controlled bidirectional file and directory names with
  `dir="auto"` plus CSS isolation, and isolate filename text inside plain-text
  tooltips without replacing the translatable accessible-name suffix.

### Fixed

- Generate the inline-style Content Security Policy SHA-256 source expression
  from the exact normalized UTF-8 stylesheet bytes emitted into each generated
  `index.html` file, preventing template whitespace from invalidating the policy.
- Preserve the exact observed filesystem identity when normalized ignore and
  sensitive-name matching rejects a whitespace-padded entry, preventing names
  such as ` .git` from surviving the later exact-name exclusion boundary.

### Tests

- Add a real generated-file regression test that independently recomputes the
  declared style hash from the emitted `<style>` text.
- Add generated-page regressions for row ordering, empty-state semantics, CSS
  cascade ordering, reduced-motion retention, visible-text decoration,
  bidirectional filename isolation, and numeric text/focus contrast thresholds.
- Add a sensitive-name regression proving that policy normalization excludes
  the exact padded filesystem identity without hiding ordinary padded names.

### Documentation

- Record the generated-page robots indexing preference, crawler-access
  prerequisite, non-security boundary, rollback contract, and current Google
  Search Central reference in `docs/doctoring/robots-indexing-preference.md`.
- Record the CSP byte-identity decision, threat boundary, verification contract,
  and current W3C Working Draft reference in `docs/doctoring`.
- Record the generated-index readability and bidirectional-isolation decisions,
  WCAG 2.2 engineering basis, Unicode UAX #9/HTML Standard basis, exact-name
  security boundary, contrast calculations, scope boundaries, and verification
  contract.
