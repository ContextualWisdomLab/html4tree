# Changelog

All notable changes to this project are documented in this file.

## [Unreleased]

### Changed

- Improve generated directory-index readability with adjacent-row separators,
  explicit light and dark empty-state text colors, and text-only hover/focus
  underlining while retaining the full interactive target's focus outline.

### Fixed

- 공백이 없는 긴 디렉토리 이름으로 인해 모바일 기기에서 레이아웃이 깨지는 현상을 방지하기 위해, 생성된 `h1` 태그에 `overflow-wrap: anywhere` 및 `word-break: break-all` 속성을 추가했습니다.
