## 2024-06-21 - Regex Compilation in Loops
**Learning:** In Kotlin, compiling regular expressions (`.toRegex()`) inside a loop over files is a significant O(N * M) performance bottleneck when processing ignore files (N files * M rules).
**Action:** Always map string rules to compiled `Regex` objects outside of the file iteration loop (O(M) compilation) to avoid unnecessary regex re-compilations.

## 2024-06-28 - Avoid O(n^2) operations when processing lists
**Learning:** Checking for containment (`in` / `!in`) within a `List` structure scales as O(n). When executed repeatedly inside a loop covering all `n` files of a directory listing, it inherently creates an O(n^2) operation, acting as a performance pitfall as directory sizes grow. Additionally, repeatedly concatenating strings (`+=`) in such loops leads to O(n^2) memory reallocation operations.
**Action:** When performing `n` membership queries against an exclusion list or tracking items, convert the collection to a `Set` for O(1) lookups. In Kotlin, use a `StringBuilder` or `.joinToString` rather than concatenating with `+=` within iterative structures to optimize performance.
