## 2024-06-21 - [XSS in Search Input]
**Vulnerability:** The application was vulnerable to Reflected Cross-Site Scripting (XSS) because user input from the search query parameter was rendered directly into the HTML response without any sanitization.
**Learning:** React's `dangerouslySetInnerHTML` or direct DOM manipulation bypasses built-in XSS protections. Even internal tools must sanitize input.
**Prevention:** Always use default React rendering (which auto-escapes) or sanitize with DOMPurify before dangerously rendering HTML.

## 2024-06-26 - [Path Traversal / Overwrite via Symbolic Links]
**Vulnerability:** The application generated `index.html` files directly inside child directories using `File(curr_dir, "index.html").writeText(...)` and also traversed symbolic links without validation. An attacker could create a symbolic link named `index.html` pointing to a critical system file (e.g., `/etc/passwd` or outside the intended directory), and when `html4tree` ran, it would overwrite that file with the generated HTML output. Also, traversing symbolic links recursively could lead to infinite loops or reading/traversing unauthorized paths.
**Learning:** `File.writeText()` blindly follows symbolic links. If a symlink with the target output name exists, the file it points to will be overwritten. Relying entirely on directory content without checking `Files.isSymbolicLink()` is a critical security risk when writing files in attacker-controlled directories.
**Prevention:**
1. Ignore symbolic links during recursive directory traversal (`if (it.isDirectory() && !Files.isSymbolicLink(it.toPath()))`).
2. Before writing the output file (`index.html`), check if it exists as a symbolic link and explicitly delete it (`if (Files.isSymbolicLink(indexFile.toPath())) { indexFile.delete() }`). This breaks the link instead of following it.
