## 2024-06-21 - Regex Compilation in Loops
**Learning:** In Kotlin, compiling regular expressions (`.toRegex()`) inside a loop over files is a significant O(N * M) performance bottleneck when processing ignore files (N files * M rules).
**Action:** Always map string rules to compiled `Regex` objects outside of the file iteration loop (O(M) compilation) to avoid unnecessary regex re-compilations.

## 2024-05-19 - String Concatenation in HTML Generation
**Learning:** Repeated `+=` concatenation while rendering directory entries copies growing strings and turns large directory output into O(N^2) work.
**Action:** Use `StringBuilder` inside the directory-entry loop and append newlines separately.
