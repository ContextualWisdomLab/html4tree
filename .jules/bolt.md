## 2024-06-25 - ⚡ Bolt: Used StringBuilder instead of String concatenation (+) in a loop
**Learning:** String concatenation inside a loop leads to quadratic time complexity O(n^2) because strings are immutable in Kotlin/Java. When the string gets bigger, creating new copies takes a lot of time. The method `process_dir` in `src/main/kotlin/html4tree/main.kt` had a loop doing string concatenation on directories with many files.
**Action:** Use `java.lang.StringBuilder` or Kotlin's `buildString` inside loops instead of `+` to maintain O(n) performance.
