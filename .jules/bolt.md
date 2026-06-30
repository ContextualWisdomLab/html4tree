## 2024-06-21 - Regex Compilation in Loops
**Learning:** In Kotlin, compiling regular expressions (`.toRegex()`) inside a loop over files is a significant O(N * M) performance bottleneck when processing ignore files (N files * M rules).
**Action:** Always map string rules to compiled `Regex` objects outside of the file iteration loop (O(M) compilation) to avoid unnecessary regex re-compilations.

## 2024-05-24 - Loop Allocation Hot Paths
**Learning:** Rendering directory entries with repeated string concatenation and list-based exclusion lookups creates avoidable allocation and lookup cost in large directories.
**Action:** Use `StringBuilder` for entry rendering and a `Set` for excluded file names.

## 2024-07-01 - Kotlin/JVM String replacement overhead for HTML rendering
**Learning:** Sequential `.replace()` calls on Kotlin strings inside hot paths like HTML escaping create multiple intermediate string instances, causing significant GC pressure.
**Action:** Replace multiple string substitution calls with a single-pass character iteration that only allocates a `StringBuilder` when an escapable character is actually encountered, turning $O(n)$ allocations into O(1) builder for strings that need escaping, and 0 allocations for normal strings.
