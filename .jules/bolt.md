## 2024-06-21 - Regex Compilation in Loops
**Learning:** In Kotlin, compiling regular expressions (`.toRegex()`) inside a loop over files is a significant O(N * M) performance bottleneck when processing ignore files (N files * M rules).
**Action:** Always map string rules to compiled `Regex` objects outside of the file iteration loop (O(M) compilation) to avoid unnecessary regex re-compilations.

## 2024-06-28 - String Concatenation in Loops
**Learning:** In `html4tree`, using `+=` string concatenation inside a loop over directory files causes unnecessary intermediate object creation and memory overhead, resulting in O(N^2) complexity. The Jacoco plugin configuration treats the implicit null check on `.listFiles()?.toMutableList()` as a partially missed branch, causing test coverage issues.
**Action:** Use `StringBuilder` for loop string building (O(N)), and split `curr_dir.listFiles()` into a clear `if (files != null)` check to satisfy Jacoco branch coverage to maintain 100%.
