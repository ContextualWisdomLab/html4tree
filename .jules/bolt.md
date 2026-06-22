## 2025-06-21 - O(N*M) Regex Compilation Bottleneck
**Learning:** In Kotlin, creating regexes in a nested loop (e.g., inside a file iteration over directory contents) causes severe O(N*M) performance degradation where N=files, M=ignore lines. Recompiling regexes is extremely expensive.
**Action:** Always map strings to compiled Regex objects outside of the loop before applying them.

## 2025-06-21 - O(N^2) String Concatenation Bottleneck
**Learning:** Using `+=` to build a large string for HTML file contents leads to quadratic performance decay as the number of lines (N) increases because strings are immutable in Kotlin/Java.
**Action:** Use `StringBuilder` to accumulate large string contents efficiently.
