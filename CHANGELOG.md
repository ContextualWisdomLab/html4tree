# Changelog

All notable changes to this project are documented in this file.

## [Unreleased]

### Added

- Emit a `noindex, nofollow` robots meta preference on every generated
  directory page, with the explicit boundary that supporting crawlers must
  first fetch the page and that confidential data still requires server-side
  protection.

### Changed

- 생성된 디렉토리 인덱스의 가독성을 향상하기 위해 인접한 항목 간 구분선, 명시적인 라이트 모드 및 다크 모드용 빈 상태(empty-state) 텍스트 색상, 그리고 전체 인터랙티브 요소의 포커스 윤곽선은 유지하면서 텍스트에만 호버(hover) 및 포커스(focus) 밑줄이 적용되도록 개선했습니다.
- 외부에서 제공되어 기본 텍스트 방향을 알 수 없는 디렉토리 및 파일 이름 텍스트에 `dir="auto"`를 적용하고, 고정된 상위 디렉토리 이동 링크와 타입 라벨 등은 페이지의 기본 한국어 텍스트 방향을 따르도록 유지했습니다.

### Fixed

- Generate the inline-style Content Security Policy SHA-256 source expression
  from the exact normalized UTF-8 stylesheet bytes emitted into each generated
  `index.html` file, preventing template whitespace from invalidating the policy.

### Tests

- Add a real generated-file regression test that independently recomputes the
  declared style hash from the emitted `<style>` text.
- 행 정렬(row ordering), 빈 상태 시맨틱(empty-state semantics), CSS 계단식 정렬(cascade ordering), 모션 감소 설정 유지(reduced-motion retention), 텍스트 전용 꾸밈(text-only decoration) 및 숫자 형태의 텍스트/포커스 대비율 테스트 등 생성 페이지 검증을 위한 회귀 테스트를 추가했습니다.
- 아랍어와 라틴어가 혼합된 디렉토리 및 파일 이름을 사용한 양방향(BiDi) 텍스트 출력 회귀 테스트를 추가했으며, 고정된 상위 탐색 경로와 라벨에 대한 경계 검증도 포함했습니다.

### Documentation

- Record the generated-page robots indexing preference, crawler-access
  prerequisite, non-security boundary, rollback contract, and current Google
  Search Central reference in `docs/doctoring/robots-indexing-preference.md`.
- Record the CSP byte-identity decision, threat boundary, verification contract,
  and current W3C Working Draft reference in `docs/doctoring`.
- 생성 인덱스의 가독성 향상 결정, WCAG 2.2 기준에 기반한 설계 원리, 대비율 계산 방식, 적용 범위 및 검증 방식을 기록했습니다.
- 외부에서 제공되어 방향을 알 수 없는 이름에 대한 `dir="auto"`의 적용 범위 및 휴리스틱 경계를 `docs/doctoring/bidirectional-generated-names.md`에 한국어로 기록했습니다.
