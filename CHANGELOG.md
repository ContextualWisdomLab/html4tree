# Changelog

## [Unreleased]
### Security (보안)
- 심볼릭 링크를 악용한 임의 파일 덮어쓰기 취약점 및 무한 루프 디렉토리 순회 방지
  - 디렉토리 순회 시 심볼릭 링크 우회: `Files.isSymbolicLink` 체크 추가
  - `index.html`은 임시 파일로 먼저 작성한 뒤 NIO move로 교체해 심볼릭 링크 대상을 덮어쓰지 않도록 변경
