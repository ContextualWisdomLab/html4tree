## 2024-06-21 - Regex Compilation in Loops
**Learning:** In Kotlin, compiling regular expressions (`.toRegex()`) inside a loop over files is a significant O(N * M) performance bottleneck when processing ignore files (N files * M rules).
**Action:** Always map string rules to compiled `Regex` objects outside of the file iteration loop (O(M) compilation) to avoid unnecessary regex re-compilations.
## 2024-07-28 - [String concatenation in Loop Optimization]
**Learning:** In Kotlin (and Java), using `+=` for string concatenation within a loop can lead to O(N^2) time complexity and excessive memory allocation due to the creation of intermediate String objects on every iteration.
**Action:** Always use `StringBuilder` for constructing strings within a loop to ensure O(N) performance and minimize garbage collection overhead.
