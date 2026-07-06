# 변경 사항

## [Unreleased]
### 보안 (Security)
- `canonicalFile` 대신 `normalize()`를 사용하여 `html4tree`의 심볼릭 링크 기반 디렉토리 탐색 우회 취약점을 수정했습니다. `canonicalFile`은 사전에 심볼릭 링크를 대상 경로로 해석하여 이어지는 `LinkOption.NOFOLLOW_LINKS` 검사를 무력화하는 문제가 있었습니다.
