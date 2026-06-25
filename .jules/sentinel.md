## 2024-06-25 - Prevent Directory Traversal through Symbolic Links
**Vulnerability:** Path Traversal via symlink parsing.
**Learning:** During directory traversal in `html4tree/main.kt`, `File.isDirectory()` returns `true` for symbolic links pointing to directories. This could allow the program to traverse outside the intended directory structure and write `index.html` files in arbitrary, potentially sensitive locations.
**Prevention:** Explicitly check for symlinks using `java.nio.file.Files.isSymbolicLink()` during recursive directory processing to ensure we do not follow them and breach directory confinement.
