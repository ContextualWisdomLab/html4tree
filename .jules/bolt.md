## 2024-06-21 - Regex Compilation in Loops
**Learning:** In Kotlin, compiling regular expressions (`.toRegex()`) inside a loop over files is a significant O(N * M) performance bottleneck when processing ignore files (N files * M rules).
**Action:** Always map string rules to compiled `Regex` objects outside of the file iteration loop (O(M) compilation) to avoid unnecessary regex re-compilations.

## 2024-05-24 - Loop Allocation Hot Paths
**Learning:** Rendering directory entries with repeated string concatenation and list-based exclusion lookups creates avoidable allocation and lookup cost in large directories.
**Action:** Use `StringBuilder` for entry rendering and a `Set` for excluded file names.

## 2024-07-26 - Intermediate String Allocations
**Learning:** Chained `.replace()` calls on strings in Kotlin (e.g. for HTML escaping) allocate an intermediate String at each step, significantly impacting performance and garbage collection on hot paths with many elements.
**Action:** Replace chained `.replace()` calls with a single-pass loop that iterates over characters once, lazily initializing a `StringBuilder` to append the transformed output.
## 2024-07-08 - URL Encoding String Allocation Bottleneck
**Learning:** `byte.toString(16).padStart(2, '0').toUpperCase()` inside a loop allocating up to 3 strings per reserved byte in a hot path causes significant GC pressure. This is a common but dangerous anti-pattern in Kotlin when processing large strings or numerous files in directory crawlers.
**Action:** Replace chained string operations with direct character mapping and bitwise operations (`ushr`, `and`) when building formatted hex output, which avoids intermediate string creation entirely. Ensure 100% branch coverage with test inputs spanning both < 10 and > 9 hex values.
## 2024-08-01 - Avoid List Sorting and Re-Iteration For Matching
**Learning:** In Kotlin, using `.sorted().forEach()` and an inner `.forEach()` over regexes to find matching files is inefficient. It allocates a new list, sorts it (O(N log N)), and evaluates every regex even after a match is found.
**Action:** Remove `.sorted()` when order doesn't matter for the operation (like adding to a Set). Use `.any()` on the regex list to short-circuit evaluation as soon as the first match is found, saving CPU cycles on N * M comparisons.
