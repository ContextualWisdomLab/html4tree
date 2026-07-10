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
## $(date +%Y-%m-%d) - [불필요한 정렬 제거 및 조기 종료로 파일 무시 로직 최적화]
**Learning:** 최종 결과가 순서가 없는 Set에 저장될 때, 입력 데이터를 정렬(.sorted())하는 것은 불필요한 O(N log N) 비용을 발생시킵니다. 또한 여러 정규식 패턴을 순회할 때 조건을 만족하면 즉시 종료(.any())하여 최악의 경우 O(N*M)가 되는 것을 방지할 수 있습니다.
**Action:** Set과 같이 순서를 보장하지 않는 자료구조를 사용할 때는 불필요한 중간 정렬을 피하고, 단순 매칭 여부만 판단할 경우 전체 순회(forEach) 대신 short-circuiting이 가능한 메서드(any, all, first 등)를 활용할 것.
