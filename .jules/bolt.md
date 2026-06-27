## 2024-06-21 - Regex Compilation in Loops
**Learning:** In Kotlin, compiling regular expressions (`.toRegex()`) inside a loop over files is a significant O(N * M) performance bottleneck when processing ignore files (N files * M rules).
**Action:** Always map string rules to compiled `Regex` objects outside of the file iteration loop (O(M) compilation) to avoid unnecessary regex re-compilations.

## 2024-06-27 - String Concatenation in Loops
**Learning:** In Kotlin, using `var l=""` and `l += ...` inside a loop (like iterating through files in a directory) causes O(N^2) complexity due to string immutability and continuous memory re-allocation.
**Action:** Always use `StringBuilder` and `append()` for concatenating strings in loops, especially for HTML generation which may contain many elements, resulting in O(N) complexity.
