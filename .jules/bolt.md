## 2024-06-21 - Regex Compilation in Loops
**Learning:** In Kotlin, compiling regular expressions (`.toRegex()`) inside a loop over files is a significant O(N * M) performance bottleneck when processing ignore files (N files * M rules).
**Action:** Always map string rules to compiled `Regex` objects outside of the file iteration loop (O(M) compilation) to avoid unnecessary regex re-compilations.

## 2024-05-24 - Loop Allocation Hot Paths
**Learning:** Rendering directory entries with repeated string concatenation and list-based exclusion lookups creates avoidable allocation and lookup cost in large directories.
**Action:** Use `StringBuilder` for entry rendering and a `Set` for excluded file names.
## 2024-05-19 - Replace custom LinkedList with ArrayDeque
**Learning:** The custom `LinkedList` implementation in `src/main/kotlin/html4tree/util.kt` had an inefficient O(N) push operation and potential state bugs.
**Action:** Replaced it with the standard `java.util.ArrayDeque` for O(1) push operations, significantly improving performance for deep directory structures, while preserving the exact same tree traversal order and logic.
