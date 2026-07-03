## 2024-06-21 - Regex Compilation in Loops
**Learning:** In Kotlin, compiling regular expressions (`.toRegex()`) inside a loop over files is a significant O(N * M) performance bottleneck when processing ignore files (N files * M rules).
**Action:** Always map string rules to compiled `Regex` objects outside of the file iteration loop (O(M) compilation) to avoid unnecessary regex re-compilations.

## 2024-05-24 - Loop Allocation Hot Paths
**Learning:** Rendering directory entries with repeated string concatenation and list-based exclusion lookups creates avoidable allocation and lookup cost in large directories.
**Action:** Use `StringBuilder` for entry rendering and a `Set` for excluded file names.

## 2024-07-03 - Inefficient String Escaping
**Learning:** Chaining `replace` calls on a string in Kotlin allocates multiple intermediate strings and causes O(N) allocation overhead for a simple escaping process.
**Action:** Replace multiple `replace` calls with a single-pass `StringBuilder` loop that checks if escaping is needed first, drastically reducing allocation and execution time.
