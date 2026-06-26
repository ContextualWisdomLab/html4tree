# 변경 사항

## [Unreleased]
### 추가됨
- 생성되는 `index.html` 파일에 접근성 및 모바일 반응성을 위한 향상된 기능 추가:
  - `<html>` 태그에 `lang="en"` 속성 추가
  - 모바일 반응성을 위한 `<meta name="viewport">` 태그 추가
  - 문자 인코딩을 명시하는 `<meta charset="UTF-8">` 태그 추가
  - 키보드 탐색 가시성을 높이기 위해 링크에 `:hover` 및 `:focus` 스타일 추가
- 전체 코드베이스(`MainKt` 및 `LinkedList`)에 대한 100% 테스트 커버리지 달성을 위한 단위 테스트 추가
- 테스트 커버리지 측정을 위한 Jacoco 플러그인 추가
