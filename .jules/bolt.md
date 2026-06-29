## 2024-06-03 - [Regex Compilation in Loops]
**Learning:** `process_ignore_file` compiled regexes inside a nested loop for every file, leading to O(N*M) complexity.
**Action:** Pre-compile regexes before the loop to reduce complexity to O(M) + matching time.
