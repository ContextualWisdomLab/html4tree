## 2024-06-21 - Regex Compilation in Loops
**Learning:** In Kotlin, compiling regular expressions (`.toRegex()`) inside a loop over files is a significant O(N * M) performance bottleneck when processing ignore files (N files * M rules).
**Action:** Always map string rules to compiled `Regex` objects outside of the file iteration loop (O(M) compilation) to avoid unnecessary regex re-compilations.

## 2024-05-24 - Loop Allocation Hot Paths
**Learning:** Rendering directory entries with repeated string concatenation and list-based exclusion lookups creates avoidable allocation and lookup cost in large directories.
**Action:** Use `StringBuilder` for entry rendering and a `Set` for excluded file names.

## 2024-07-25 - Avoid Chained String Replacements
**Learning:** In Kotlin, using multiple chained `.replace()` calls on a String for frequent operations (like HTML escaping) is inefficient due to multiple intermediate allocations.
**Action:** Use a single-pass loop with a lazily-initialized `StringBuilder` to prevent intermediate string allocations, significantly improving performance on hot paths.
