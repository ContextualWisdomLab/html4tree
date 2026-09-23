# Changelog

All notable changes to this project are documented in this file.

## [Unreleased]

### Security

- 사용자 제어 파일·디렉터리 이름의 BiDi 표시 혼동을 줄이기 위해 Unicode bidirectional ordering control(ALM/LRM/RLM, embedding/override, isolate)을 가시적인 escape 문자열로 변환하고, 생성 HTML의 표시 이름을 FSI/PDI로 격리합니다. 이 변경은 표시 경계 hardening이며 기만적인 파일 실행 자체를 차단한다고 보장하지 않습니다.

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
