# Changelog

## Unreleased
- 🛡️ Sentinel: [HIGH] `File.canonicalFile` 대신 `.absoluteFile.toPath().normalize().toFile()`을 사용하여 심볼릭 링크를 통해 의도하지 않은 디렉토리로 이동하는 경로 탐색(Path Traversal) 취약점을 수정했습니다.
