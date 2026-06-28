## 2024-06-21 - [html4tree] Unsanitized Filenames in Auto-Generated HTML
**Vulnerability:** XSS via Malicious File/Directory Names
**Learning:** Tools that auto-generate static HTML pages from local file systems often overlook input sanitization, implicitly trusting local file paths. If these generated pages are hosted or shared, an attacker can create files with names like `<script>alert(1)</script>` to execute arbitrary JavaScript in the context of the user viewing the generated index.
**Prevention:** Always HTML-encode variable data injected into HTML templates, and URL-encode data used in `href` attributes, regardless of the data's origin (even if it's "just" the local file system). Additionally, ensure HTML attributes like `href` are properly quoted to prevent attribute breakout.

## 2024-06-26 - [Path Traversal / Overwrite via Symbolic Links]
**Vulnerability:** The application generated `index.html` files directly inside child directories using `File(curr_dir, "index.html").writeText(...)` and also traversed symbolic links without validation. An attacker could create a symbolic link named `index.html` pointing to a critical system file (e.g., `/etc/passwd` or outside the intended directory), and when `html4tree` ran, it would overwrite that file with the generated HTML output. Also, traversing symbolic links recursively could lead to infinite loops or reading/traversing unauthorized paths.
**Learning:** `File.writeText()` blindly follows symbolic links. If a symlink with the target output name exists, the file it points to will be overwritten. Relying entirely on directory content without checking `Files.isSymbolicLink()` is a critical security risk when writing files in attacker-controlled directories.
**Prevention:**
1. Ignore symbolic links during recursive directory traversal (`if (it.isDirectory() && !Files.isSymbolicLink(it.toPath()))`).
2. Write generated HTML to a temporary file in the target directory, then move it over `index.html` with NIO `Files.move(..., REPLACE_EXISTING)`. This replaces an `index.html` symlink itself instead of following it to overwrite the link target.
