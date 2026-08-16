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
- Show each file's exact byte size (IEC binary prefixes above 1023 bytes)
  and UTC last-modified time so similarly named files can be distinguished.
- Replace bidirectional format controls in displayed names with U+FFFD
  and keep the real filesystem name in `href`, so Trojan Source extension
  hiding cannot look like a safe file.

### Changed

- Reuse one directory-name snapshot in `process_ignore_file` when the caller
  omits names, so `.html4ignore` globs and default sensitive-name filtering
  observe the same listing instead of calling `File.list()` twice.
- Share one `File.listFiles()` snapshot in the `process_dir` fallback so
  ignore filtering and the generated page hide the same names. An unreadable
  listing stays empty and does not call `File.list()`.
- Pass an empty name and file snapshot from `crawl_directories` when
  `listFiles()` returns null, so ignore filtering and render do not open
  the directory again and a later successful listing cannot appear on the
  page.
- Keep `#459` translatable `.visually-hidden` type labels and underline
  `.entry-name` on hover/focus instead of the hidden last child.
- Improve generated directory-index readability with adjacent-row separators,
  explicit light and dark empty-state text colors, and text-only hover/focus
  underlining while retaining the full interactive target's focus outline.

### Fixed

- Generate the inline-style Content Security Policy SHA-256 source expression
  from the exact normalized UTF-8 stylesheet bytes emitted into each generated
  `index.html` file, preventing template whitespace from invalidating the policy.

### Tests

- Lock the ignore-listing snapshot: at most one `File.list()` when names are
  omitted, zero listings when names are supplied, default exclusions when
  `list()` is null, a generated page that keeps `minutes.txt` as
  `href="./minutes.txt"` while hiding `scratch.tmp` and `.env`, one
  `listFiles()` and zero `list()` on the `process_dir` fallback, an
  empty page when `listFiles()` returns null, and the same empty page
  with zero `list()` / `listFiles()` when the crawl snapshot is null.
- Add generated-page regressions that open real Arabic and Hebrew filenames
  and assert isolated markup, percent-encoded hrefs, and FSI/PDI titles.
- Add a generated-page regression that a real `invoice.txt` + RLO + `exe`
  file shows U+FFFD, keeps the encoded `href`, and does not strip FSI/PDI
  from `escapeHtml()`.
- Add a generated-page regression that `minutes.txt` containing
  `hello world` emits `11 B` and a `<time datetime>` matching the
  filesystem mtime.
- Add a listing-boundary test that padded sensitive names stay out of
  `index.html` while ordinary padded names remain visible.
- Add a real generated-file regression test that independently recomputes the
  declared style hash from the emitted `<style>` text.
- Add generated-page regressions for row ordering, empty-state semantics, CSS
  cascade ordering, reduced-motion retention, text-only decoration, and numeric
  text/focus contrast thresholds.

### Documentation

- Record the ignore-listing snapshot decision, glob (not regex) contract,
  TOCTOU rationale, `process_dir` shared `listFiles()` snapshot, crawl
  empty-array forwarding, and generated-page hide/keep verification in
  `docs/doctoring/ignore-listing-snapshot.md`.
- Record the generated-page robots indexing preference, crawler-access
  prerequisite, non-security boundary, rollback contract, and current Google
  Search Central reference in `docs/doctoring/robots-indexing-preference.md`.
- Record the CSP byte-identity decision, threat boundary, verification contract,
  and current W3C Working Draft reference in `docs/doctoring`.
- Record the generated-index readability decision, WCAG 2.2 engineering basis,
  contrast calculations, scope boundaries, and verification contract.
- Record the bidirectional isolation decision, HTML `dir=auto` mapping, and
  Unicode UAX #9 citations in `docs/doctoring/bidi-isolation.md`.
- Record bidirectional-control neutralization (UTS #39, UTR #36, Trojan
  Source) in `docs/doctoring/bidi-control-neutralization.md`.
- Record size and last-modified listing metadata (Apache FancyIndexing,
  IEC 80000-13, ISO 8601-1) in `docs/doctoring/listing-entry-metadata.md`.
