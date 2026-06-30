## 2024-06-21 - Regex Compilation in Loops
**Learning:** In Kotlin, compiling regular expressions (`.toRegex()`) inside a loop over files is a significant O(N * M) performance bottleneck when processing ignore files (N files * M rules).
**Action:** Always map string rules to compiled `Regex` objects outside of the file iteration loop (O(M) compilation) to avoid unnecessary regex re-compilations.
## 2024-06-25 - Collection Lookup Performance in Loops
**Learning:** Checking for file exclusion (`in exclude`) inside a loop iterating over directory contents can be a significant bottleneck (O(N) lookup for each file) if the `exclude` collection is a `List`.
**Action:** Always prefer using `Set` (e.g., `mutableSetOf()`) over `List` for collections used primarily for lookup/containment checks inside loops, changing the lookup from O(N) to O(1).
