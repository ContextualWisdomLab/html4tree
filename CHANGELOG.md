# Changelog

All notable changes to this project are documented in this file.

## [Unreleased]

### Added

- Isolate Arabic, Hebrew, and mixed-script filenames with `dir="auto"`,
  `unicode-bidi: isolate`, and First Strong Isolate marks in `title`
  attributes so Korean type labels stay readable.
- Publish listing color tokens (`--listing-*`) so the repeating directory
  row can be restyled without copying hex values through the stylesheet.
- Emit a `noindex, nofollow` robots meta preference on every generated
  directory page, with the explicit boundary that supporting crawlers must
  first fetch the page and that confidential data still requires server-side
  protection.

### Changed

- Keep `#459` translatable `.visually-hidden` type labels and underline
  `.entry-name` on hover/focus instead of the hidden last child.

### Changed

- Improve generated directory-index readability with adjacent-row separators,
  explicit light and dark empty-state text colors, and text-only hover/focus
  underlining while retaining the full interactive target's focus outline.

### Fixed

- Generate the inline-style Content Security Policy SHA-256 source expression
  from the exact normalized UTF-8 stylesheet bytes emitted into each generated
  `index.html` file, preventing template whitespace from invalidating the policy.

### Tests

- Add generated-page regressions that open real Arabic and Hebrew filenames
  and assert isolated markup, percent-encoded hrefs, and FSI/PDI titles.
- Add a listing-boundary test that padded sensitive names stay out of
  `index.html` while ordinary padded names remain visible.
- Add a real generated-file regression test that independently recomputes the
  declared style hash from the emitted `<style>` text.
- Add generated-page regressions for row ordering, empty-state semantics, CSS
  cascade ordering, reduced-motion retention, text-only decoration, and numeric
  text/focus contrast thresholds.

### Documentation

- Record the generated-page robots indexing preference, crawler-access
  prerequisite, non-security boundary, rollback contract, and current Google
  Search Central reference in `docs/doctoring/robots-indexing-preference.md`.
- Record the CSP byte-identity decision, threat boundary, verification contract,
  and current W3C Working Draft reference in `docs/doctoring`.
- Record the generated-index readability decision, WCAG 2.2 engineering basis,
  contrast calculations, scope boundaries, and verification contract.
- Record the bidirectional isolation decision, HTML `dir=auto` mapping, and
  Unicode UAX #9 citations in `docs/doctoring/bidi-isolation.md`.
