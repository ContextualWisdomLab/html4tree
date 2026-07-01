## 2024-07-01 - 심볼릭 링크(Symlink) 검사 우회 취약점
**Vulnerability:** `canonicalFile`을 호출한 후 `LinkOption.NOFOLLOW_LINKS` 옵션을 사용해 디렉토리 검증을 시도하여 심볼릭 링크 검사가 무력화됨. (Path Traversal 우려)
**Learning:** File 객체의 `canonicalFile`을 호출하면 내부적으로 심볼릭 링크가 평가되어 실제 대상 경로로 변환됨. 변환된 경로에 대해 심볼릭 링크 여부를 검사하면 항상 "심볼릭 링크가 아님"으로 평가됨.
**Prevention:** 심볼릭 링크 여부나 검사를 수행할 때는 대상 경로로 변환(`canonicalFile`)하기 전에 원본 파일 경로 객체 자체를 기반으로 검사해야 함.
