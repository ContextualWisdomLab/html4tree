## 2024-05-20 - [Redundant Directory Listing I/O]
**Learning:** `File.list()` is an expensive I/O operation. In Kotlin, when checking ignore patterns and iterating directories, making multiple calls to `list()` adds significant latency.
**Action:** Always fetch the directory list once and cache it in a local variable before iterating or applying multiple filters to avoid redundant system I/O.
