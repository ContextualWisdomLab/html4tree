## 2024-05-24 - Avoid O(N^2) String Concatenation and Repeated Regex Compilation in Kotlin Loops
**Learning:** In Kotlin, using `+=` to concatenate strings inside loops creates large numbers of intermediate String objects, significantly degrading performance due to O(N^2) time complexity. Additionally, compiling `Regex` patterns inside nested loops scales poorly with large numbers of files.
**Action:** Always use `StringBuilder` for loop-based string construction and pre-compile regular expressions outside of iterative structures (e.g., directory traversals).
