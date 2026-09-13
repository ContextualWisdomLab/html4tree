# Changelog

### Security Fixes
- 양방향(BiDi) 텍스트 스푸핑 취약점 수정: 사용자 입력 텍스트에 HTML 방향성 고립을 적용하여 악의적인 RTL(Right-To-Left) 파일 이름으로 인한 경로 노출 및 조작을 방지


All notable changes to this project are documented in this file.

## [Unreleased]

### Added

- Emit a `noindex, nofollow` robots meta preference on every generated
  directory page, with the explicit boundary that supporting crawlers must
  first fetch the page and that confidential data still requires server-side
  protection.

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

- Record the generated-page robots indexing preference, crawler-access
  prerequisite, non-security boundary, rollback contract, and current Google
  Search Central reference in `docs/doctoring/robots-indexing-preference.md`.
- Record the CSP byte-identity decision, threat boundary, verification contract,
  and current W3C Working Draft reference in `docs/doctoring`.
- Record the generated-index readability decision, WCAG 2.2 engineering basis,
  contrast calculations, scope boundaries, and verification contract.
