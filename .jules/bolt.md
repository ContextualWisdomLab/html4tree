## 2024-06-24 - [Kotlin Regex Compilation in Loops]
**Learning:** In Kotlin, creating a regex pattern using `toRegex()` inside a loop causes redundant, expensive recompilation of the pattern object for each iteration, creating a significant O(N*M) bottleneck when scanning large file lists.
**Action:** Always extract regex string compilation using `toRegex()` to the outer scope before any iteration, allowing the pattern to be reused.
