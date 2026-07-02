## 2024-06-21 - Regex Compilation in Loops
**Learning:** In Kotlin, compiling regular expressions (`.toRegex()`) inside a loop over files is a significant O(N * M) performance bottleneck when processing ignore files (N files * M rules).
**Action:** Always map string rules to compiled `Regex` objects outside of the file iteration loop (O(M) compilation) to avoid unnecessary regex re-compilations.

## 2024-05-24 - Loop Allocation Hot Paths
**Learning:** Rendering directory entries with repeated string concatenation and list-based exclusion lookups creates avoidable allocation and lookup cost in large directories.
**Action:** Use `StringBuilder` for entry rendering and a `Set` for excluded file names.

## 2024-07-02 - Chained String Replaces
**Learning:** Using multiple chained `.replace()` calls on a Kotlin String (e.g., for HTML escaping) allocates new intermediate strings and arrays for every replacement pass, causing significant GC pressure and CPU overhead when called frequently on long lists.
**Action:** Replace chained `.replace()` calls with a single-pass loop over the string characters. Use a lazily-initialized `StringBuilder` to append escaped values and original characters, avoiding allocations completely on the fast path (when no escaping is needed).
