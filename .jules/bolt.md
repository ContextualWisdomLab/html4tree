## 2024-06-21 - Regex Compilation in Loops
**Learning:** In Kotlin, compiling regular expressions (`.toRegex()`) inside a loop over files is a significant O(N * M) performance bottleneck when processing ignore files (N files * M rules).
**Action:** Always map string rules to compiled `Regex` objects outside of the file iteration loop (O(M) compilation) to avoid unnecessary regex re-compilations.

## 2024-05-24 - Loop Allocation Hot Paths
**Learning:** Rendering directory entries with repeated string concatenation and list-based exclusion lookups creates avoidable allocation and lookup cost in large directories.
**Action:** Use `StringBuilder` for entry rendering and a `Set` for excluded file names.
## 2024-07-04 - Optimize `process_ignore_file` performance
**Learning:** In operations dealing with file exclusions and regex matching, unnecessarily sorting the file list introduces an O(N log N) penalty. Evaluating all exclusion regexes even after a match occurs incurs an O(N * M) penalty, especially noticeable with many files or complex regexes.
**Action:** Avoid sorting operations when only checking for existence or matching within a set. Always use early exits (`break`) in loops once a condition is successfully met to prevent redundant checks.
