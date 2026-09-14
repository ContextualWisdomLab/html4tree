## 2024-05-18 - TOCTOU 및 정책 파일 접근 우회 방지
**Vulnerability:** `.html4ignore` 등의 보안 정책 파일이 존재하지만 디렉토리의 읽기 권한이나 기타 파일 시스템 조건으로 인해 읽을 수 없는 경우, 이전에 설정된 파일들을 무시하지 않고 하위 디렉토리가 외부에 노출될 수 있는 Fail-Open 취약점이 존재했습니다.
**Learning:** 기존 로직은 `.html4ignore`의 접근 실패(예: 권한 문제나 `ignore_file.isFile`, `!Files.isSymbolicLink(ignore_file.toPath())`, `ignore_file.canRead()`, 크기 제한 등의 검증 실패) 시 단순히 빈 예외 처리를 하고 모든 파일을 인덱스에 노출하는 Fail-Open 방식으로 동작하여 중요 파일 및 디렉토리가 노출되는 위험이 있었습니다.
**Prevention:** 보안 정책 적용이 누락되는 것을 막기 위해 정책 파일 존재 여부를 먼저 확인하고, 접근이 불가능하거나 검증 조건(심볼릭 링크 여부 등)을 만족하지 못할 경우 `IgnoreFileReadException`을 던지는 방식으로 변경하여 Fail-Closed 되도록 하였습니다. 또한 상위 레벨에서 예외를 캐치할 때 `null`로 처리하여 해당 디렉토리 전체가 노출되지 않도록 막았습니다.
