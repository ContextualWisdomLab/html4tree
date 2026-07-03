## 2024-06-21 - Regex Compilation in Loops
**Learning:** In Kotlin, compiling regular expressions (`.toRegex()`) inside a loop over files is a significant O(N * M) performance bottleneck when processing ignore files (N files * M rules).
**Action:** Always map string rules to compiled `Regex` objects outside of the file iteration loop (O(M) compilation) to avoid unnecessary regex re-compilations.

## 2024-05-24 - Loop Allocation Hot Paths
**Learning:** Rendering directory entries with repeated string concatenation and list-based exclusion lookups creates avoidable allocation and lookup cost in large directories.
**Action:** Use `StringBuilder` for entry rendering and a `Set` for excluded file names.
## 2024-05-23 - [성능 최적화] String.replace 체이닝의 오버헤드와 단일 패스 처리
**Learning:** 여러 개의 `String.replace()`를 체이닝하여 호출하면 각 호출마다 새로운 중간 문자열 객체가 할당되어 불필요한 메모리 사용량 증가와 가비지 컬렉션 부하를 일으킵니다. 특히 문자열 이스케이핑처럼 입력 문자열의 길이가 길거나 빈번하게 호출되는 경우 성능 병목이 발생할 수 있습니다.
**Action:** 단순히 여러 번 대체해야 할 문자열이 있다면 정규 표현식 또는 `StringBuilder`를 사용하여 단일 패스로 처리하는 방식을 고려해야 합니다. 이스케이핑처럼 특정 문자들을 단일 문자로 변경하는 경우에는 미리 검사하고 1회만 할당하여 순회하는 방식이 압도적으로 빠릅니다.
