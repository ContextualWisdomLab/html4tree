# CHANGELOG

## 변경 사항

- **성능 최적화**: `process_ignore_file` 함수에서 정규표현식 컴파일(Regex compilation)이 매 파일마다 반복해서 수행되던 병목 현상을 해결했습니다. 정규표현식을 바깥쪽 루프에서 미리 컴파일하도록 최적화하여, 기존의 $O(\text{파일 수} \times \text{정규식 수})$ 복잡도를 $O(\text{정규식 수}) + \text{매칭 시간}$으로 크게 향상시켰습니다.
- **테스트 커버리지 100% 달성**: JaCoCo를 통해 `html4tree`의 `MainKt` 및 `LinkedList` 등에 대한 테스트 코드를 작성하고, Line Coverage 100%, Branch Coverage 100%를 달성했습니다 (`MainTest.kt`, `UtilTest.kt` 추가).
- 불필요한 `while` 루프 안의 조건절 중복 체크를 제거하여 커버리지 도달을 개선했습니다.
