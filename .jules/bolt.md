## 2024-06-21 - Regex Compilation in Loops
**Learning:** In Kotlin, compiling regular expressions (`.toRegex()`) inside a loop over files is a significant O(N * M) performance bottleneck when processing ignore files (N files * M rules).
**Action:** Always map string rules to compiled `Regex` objects outside of the file iteration loop (O(M) compilation) to avoid unnecessary regex re-compilations.

## 2024-05-24 - Kotlin String Concatenation Bottleneck
**Learning:** In Kotlin (as in Java), repeatedly using the `+=` operator for string concatenation within a loop leads to O(N^2) time complexity and massive memory reallocation overhead because strings are immutable. This becomes a major bottleneck when dynamically rendering HTML for directories containing a large number of files.
**Action:** Always prefer `StringBuilder` when concatenating strings within a loop to maintain O(N) complexity and minimize memory allocations.
