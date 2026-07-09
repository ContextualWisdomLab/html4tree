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

## 2024-07-29 - Excessive File Object and I/O Operations in Filter Loops
**Learning:** In directory crawlers, calling `.listFiles()` loads `File` objects for every item, and subsequently calling `it.toPath()` and expensive I/O operations (`Files.isDirectory`, `Files.isSymbolicLink`) *before* checking exclusions creates significant overhead on large directories with many excluded files.
**Action:** Always check file names against exclusion sets (`if (fileName !in exclude)`) *before* allocating `Path` objects or executing I/O checks.

## 2024-07-29 - Unnecessary Sorting in Set Accumulation
**Learning:** Calling `.sorted()` on a directory listing (`curr_dir.list()?.sorted()?.forEach`) before adding elements to a `Set` or checking against a list of RegExes wastes O(N log N) time and allocates unnecessary arrays.
**Action:** Omit `.sorted()` when order is irrelevant (e.g., when populating an exclusion `Set`) and use `.any` to short-circuit regex checking.
