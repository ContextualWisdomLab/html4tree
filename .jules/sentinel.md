## 2024-10-24 - [심볼릭 링크 탐색(Path Traversal) 방지 및 예외 처리 강화]
**Vulnerability:** 악의적인 사용자가 생성한 심볼릭 링크를 따라가면서 임의의 경로(예: `/tmp`)에 파일을 덮어쓰거나(Arbitrary File Write) 무한 루프에 빠질 수 있는 Path Traversal 취약점이 존재했습니다. 또한 접근 불가능한 디렉토리에 접근 시 `NullPointerException/IllegalStateException` 에러로 프로그램이 크래시되는 DoS 문제가 있었습니다.
**Learning:** `listFiles()`로 디렉토리를 탐색할 때, 순회하는 대상이 심볼릭 링크인지 확인하는 로직이 빠져있었고, 권한 없음 등으로 인한 `null` 반환을 고려하지 않아 크래시가 발생할 수 있음을 확인했습니다.
**Prevention:** `java.nio.file.Files.isSymbolicLink(it.toPath())`를 이용해 심볼릭 링크를 명시적으로 무시하고, `listFiles()`의 반환값이 `null`인지 안전하게 처리(safe call 및 elvis operator)하며, 인덱스 파일 생성 시 try-catch 블록으로 묶어 권한 오류에도 프로그램이 정상 동작하도록 해야 합니다.
