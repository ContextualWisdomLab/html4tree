## 2024-06-21 - Regex Compilation in Loops
**Learning:** In Kotlin, compiling regular expressions (`.toRegex()`) inside a loop over files is a significant O(N * M) performance bottleneck when processing ignore files (N files * M rules).
**Action:** Always map string rules to compiled `Regex` objects outside of the file iteration loop (O(M) compilation) to avoid unnecessary regex re-compilations.

## 2024-05-24 - Loop Allocation Hot Paths
**Learning:** Rendering directory entries with repeated string concatenation and list-based exclusion lookups creates avoidable allocation and lookup cost in large directories.
**Action:** Use `StringBuilder` for entry rendering and a `Set` for excluded file names.

## 2024-07-26 - Intermediate String Allocations
**Learning:** Chained `.replace()` calls on strings in Kotlin (e.g. for HTML escaping) allocate an intermediate String at each step, significantly impacting performance and garbage collection on hot paths with many elements.
**Action:** Replace chained `.replace()` calls with a single-pass loop that iterates over characters once, lazily initializing a `StringBuilder` to append the transformed output.
## 2024-03-20 - [String.urlEncodePath 최적화]
**Learning:** `String.urlEncodePath`에서 기존 `forEach` 문과 매 바이트마다 호출되던 `toString(16).padStart` 방식이 불필요한 String 인스턴스를 대량으로 생성하여 성능 병목이 되고 있었습니다.
**Action:** `StringBuilder`의 크기를 사전 할당하고, hex 문자열 배열 조회를 통한 비트 연산으로 대체하여 중간 String 생성을 제거함으로써 실행 시간을 절반 이하(약 55% 개선)로 단축할 수 있습니다.
