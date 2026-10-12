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

- **접근성 및 UX 수정:** 링크에 추가된 `.visually-hidden` 스크린 리더 텍스트로 인해, 시각적으로 보이는 파일/디렉토리 텍스트의 마우스 오버(Hover) 시 밑줄 효과가 나타나지 않던 버그를 수정했습니다. CSS 가상 클래스 타겟팅을 `:last-child`에서 `:nth-child(2)`로 변경하여 시각적 피드백을 복구했습니다.
