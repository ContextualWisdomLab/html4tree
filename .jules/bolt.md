## 2024-06-21 - Regex Compilation in Loops
**Learning:** In Kotlin, compiling regular expressions (`.toRegex()`) inside a loop over files is a significant O(N * M) performance bottleneck when processing ignore files (N files * M rules).
**Action:** Always map string rules to compiled `Regex` objects outside of the file iteration loop (O(M) compilation) to avoid unnecessary regex re-compilations.

## 2024-05-24 - Loop Allocation Hot Paths
**Learning:** Rendering directory entries with repeated string concatenation and list-based exclusion lookups creates avoidable allocation and lookup cost in large directories.
**Action:** Use `StringBuilder` for entry rendering and a `Set` for excluded file names.

## 2024-07-26 - Intermediate String Allocations
**Learning:** Chained `.replace()` calls on strings in Kotlin (e.g. for HTML escaping) allocate an intermediate String at each step, significantly impacting performance and garbage collection on hot paths with many elements.
**Action:** Replace chained `.replace()` calls with a single-pass loop that iterates over characters once, lazily initializing a `StringBuilder` to append the transformed output.

## 2024-07-28 - Number Formatting Object Allocations
**Learning:** Using built-in chained methods like `.toString(16).padStart(2, '0').toUpperCase()` inside a tight loop for URL encoding bytes creates excessive short-lived `String` objects, degrading performance and increasing GC pressure.
**Action:** Replace high-frequency byte-to-hex formatting with direct lookup arrays and bitwise operations (`hex[byte ushr 4]` and `hex[byte and 0x0F]`) to emit characters directly without intermediate objects.
