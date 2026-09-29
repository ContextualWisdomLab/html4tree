# Changelog

All notable changes to this project are documented in this file.

## [Unreleased]

### Added

- 🛡️ Sentinel: POSIX 파일 시스템에서 HTML 생성 시, 임시 `index.html` 파일을 전체 읽기 권한(`rw-r--r--`)으로 생성하도록 변경. 기본적으로 `Files.createTempFile`은 `rw-------` 권한으로 파일을 생성하는데, 원자적 교체가 실패하고 `Files.move(REPLACE_EXISTING)` 백폴백이 작동할 경우 이 제한된 권한이 그대로 유지됨. 이로 인해 그룹이나 전체 읽기 권한에 의존하는 웹 서버에서 403 Forbidden Error(서비스 거부, DoS)가 발생하는 것을 방지하기 위해 권한을 명시적으로 설정.
- 올바른 권한 대체를 검증하기 위한 `testWriteIndexFileCreatesWorldReadableTempFileWhenPosixSupported` 테스트 케이스 추가.
- `testWriteIndexFileWithoutPosixSupport` 테스트 케이스 추가.

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
