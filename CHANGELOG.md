# Changelog

## [Unreleased]
### Added
- JaCoCo 플러그인을 통한 테스트 커버리지 측정 환경 구축
- `MainTest.kt`, `UtilTest.kt` 테스트 추가로 INSTRUCTION 기준 100% 커버리지 달성
- `.Jules/palette.md` 추가 (접근성 관련 학습 기록)

### Changed
- 생성되는 HTML 문서에 모바일 뷰포트 메타 태그(`viewport`), 문자 인코딩 메타 태그(`charset`), 그리고 언어 속성(`lang="en"`) 추가
- 시맨틱 마크업 향상을 위해 주요 콘텐츠를 `<main>`으로 감싸고, 디렉토리 링크 리스트를 `<nav aria-label="Directory navigation">`로 묶음
- 링크 `href` 속성 값에 큰따옴표 추가 적용
- `main.kt` 와 `util.kt` 내부의 null 안정성 및 edge-case(예: 빈 디렉토리, 널 반환 등) 처리 개선 (분기 테스트 커버리지 증대)
