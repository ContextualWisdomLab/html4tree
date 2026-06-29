## 2024-05-24 - Optimize string concatenation in `process_dir` loop
**Learning:** String concatenation using `+=` inside loops where potentially thousands of string appending occurs causes performance issues due to excessive memory reallocation and copying. Kotlin's `StringBuilder` drastically improves performance.
**Action:** Replace string accumulation with `+=` inside loops with `StringBuilder.append()` when generating large dynamic strings like HTML.
