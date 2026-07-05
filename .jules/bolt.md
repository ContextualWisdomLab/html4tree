## 2024-06-21 - Regex Compilation in Loops
**Learning:** In Kotlin, compiling regular expressions (`.toRegex()`) inside a loop over files is a significant O(N * M) performance bottleneck when processing ignore files (N files * M rules).
**Action:** Always map string rules to compiled `Regex` objects outside of the file iteration loop (O(M) compilation) to avoid unnecessary regex re-compilations.

## 2024-05-24 - Loop Allocation Hot Paths
**Learning:** Rendering directory entries with repeated string concatenation and list-based exclusion lookups creates avoidable allocation and lookup cost in large directories.
**Action:** Use `StringBuilder` for entry rendering and a `Set` for excluded file names.

## 2026-07-05 - [단일 패스(single-pass) 문자열 처리 최적화 패턴]
**Learning:** 여러 번 체이닝된 `String.replace()` 호출을 사용하는 것은 중간에 필요 없는 임시 `String` 객체를 다수 생성하게 되어, 디렉토리 트리 순회 등 수많은 문자열 처리가 반복될 때 심각한 가비지 컬렉션(GC) 부하와 메모리 압박을 야기할 수 있는 안티 패턴이다.
**Action:** `replace()` 체이닝 대신 단일 패스(single-pass)로 순회하는 `StringBuilder` 기반 루프를 작성하고, 변경이 아예 필요 없는 문자열의 경우 객체 할당 자체를 피하는 '초기 반환(early return)' 검증 로직을 추가하여 성능을 최적화할 것.
