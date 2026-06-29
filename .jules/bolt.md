## 2024-05-18 - StringBuilder over String Concatenation in Kotlin
**Learning:** In Kotlin (and Java), using `+=` for string concatenation within a loop has an O(N²) time complexity due to the immutability of `String`, which requires creating a new `String` and copying the contents on every iteration. This can become a significant bottleneck when processing large collections (like thousands of directory files).
**Action:** Always prefer using a `StringBuilder` (and ideally initializing it with an estimated capacity) for string accumulation inside loops to maintain O(N) performance and avoid unnecessary memory allocations.

## 2024-05-18 - StringBuilder over String Concatenation in Kotlin
**Learning:** In Kotlin (and Java), using `+=` for string concatenation within a loop has an O(N²) time complexity due to the immutability of `String`, which requires creating a new `String` and copying the contents on every iteration. This can become a significant bottleneck when processing large collections (like thousands of directory files).
**Action:** Always prefer using a `StringBuilder` (and ideally initializing it with an estimated capacity) for string accumulation inside loops to maintain O(N) performance and avoid unnecessary memory allocations.
