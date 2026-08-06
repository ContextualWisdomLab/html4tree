# Changelog

All notable changes to this project are documented in this file.

## [Unreleased]

### Changed

- Improve generated directory-index readability with adjacent-row separators,
  explicit light and dark empty-state text colors, and text-only hover/focus
  underlining while retaining the full interactive target's focus outline.

### Fixed

- Generate the inline-style Content Security Policy SHA-256 source expression
  from the exact normalized UTF-8 stylesheet bytes emitted into each generated
  `index.html` file, preventing template whitespace from invalidating the policy.

### Tests

- Add a real generated-file regression test that independently recomputes the
  declared style hash from the emitted `<style>` text.
- Add generated-page regressions for row ordering, empty-state semantics, CSS
  cascade ordering, reduced-motion retention, text-only decoration, and numeric
  text/focus contrast thresholds.

### Documentation

- Record the CSP byte-identity decision, threat boundary, verification contract,
  and current W3C Working Draft reference in `docs/doctoring`.
- Record the generated-index readability decision, WCAG 2.2 engineering basis,
  contrast calculations, scope boundaries, and verification contract.
