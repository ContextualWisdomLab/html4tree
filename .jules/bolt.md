## 2024-06-21 - Regex Compilation in Loops
**Learning:** In Kotlin, compiling regular expressions (`.toRegex()`) inside a loop over files is a significant O(N * M) performance bottleneck when processing ignore files (N files * M rules).
**Action:** Always map string rules to compiled `Regex` objects outside of the file iteration loop (O(M) compilation) to avoid unnecessary regex re-compilations.

## 2024-06-27 - StringBuilder for Large HTML generation
**Learning:** String concatenation inside a loop over a potentially large number of elements (like directory listings) is very slow in Kotlin/Java due to immutability of Strings creating many intermediate objects.
**Action:** Replace `var str = ""` and `str += "..."` inside the file iteration loop with `val sb = StringBuilder()` and `sb.append("...")` when dealing with potentially large directories to prevent O(n^2) like behavior in string allocation and copying overhead.
