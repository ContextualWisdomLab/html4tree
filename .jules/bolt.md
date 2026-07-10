## 2024-06-21 - Regex Compilation in Loops
**Learning:** In Kotlin, compiling regular expressions (`.toRegex()`) inside a loop over files is a significant O(N * M) performance bottleneck when processing ignore files (N files * M rules).
**Action:** Always map string rules to compiled `Regex` objects outside of the file iteration loop (O(M) compilation) to avoid unnecessary regex re-compilations.

## 2024-05-24 - Loop Allocation Hot Paths
**Learning:** Rendering directory entries with repeated string concatenation and list-based exclusion lookups creates avoidable allocation and lookup cost in large directories.
**Action:** Use `StringBuilder` for entry rendering and a `Set` for excluded file names.

## 2024-07-26 - Intermediate String Allocations
**Learning:** Chained `.replace()` calls on strings in Kotlin (e.g. for HTML escaping) allocate an intermediate String at each step, significantly impacting performance and garbage collection on hot paths with many elements.
**Action:** Replace chained `.replace()` calls with a single-pass loop that iterates over characters once, lazily initializing a `StringBuilder` to append the transformed output.
## 2024-07-08 - URL Encoding String Allocation Bottleneck
**Learning:** `byte.toString(16).padStart(2, '0').toUpperCase()` inside a loop allocating up to 3 strings per reserved byte in a hot path causes significant GC pressure. This is a common but dangerous anti-pattern in Kotlin when processing large strings or numerous files in directory crawlers.
**Action:** Replace chained string operations with direct character mapping and bitwise operations (`ushr`, `and`) when building formatted hex output, which avoids intermediate string creation entirely. Ensure 100% branch coverage with test inputs spanning both < 10 and > 9 hex values.

## 2024-07-28 - 디렉토리 탐색 성능 최적화
**Learning:** 디렉토리 엔트리를 순회할 때 제외(exclude) 대상인 파일에 대해 불필요한 `Path` 객체 생성과 `Files.isDirectory` 파일 시스템 I/O를 호출하는 것은 심각한 성능 저하를 초래합니다. 또한, 순서가 무의미한 제외 목록(`Set`)을 구성하기 위해 `.sorted()`를 호출하는 것은 O(N log N)의 불필요한 오버헤드를 더합니다.
**Action:** 항상 제외 조건(`fileName !in exclude`)을 루프 진입 즉시 확인하여 조기 반환하고, 결과를 정렬할 필요가 없는 경우에는 `.sorted()` 호출을 제거하여 디렉토리 처리 성능을 향상시킵니다.
