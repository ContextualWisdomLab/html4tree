# Changelog

## [Unreleased]
### Security (보안)
- 심볼릭 링크를 악용한 임의 파일 덮어쓰기 취약점 및 무한 루프 디렉토리 순회 방지
  - 디렉토리 순회 시 심볼릭 링크 우회: `Files.isSymbolicLink` 체크 추가
  - `index.html` 작성 전, 동일 이름의 심볼릭 링크가 존재할 경우 이를 삭제하고 파일 생성
