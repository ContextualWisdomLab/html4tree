## 2024-06-21 - [html4tree] Unsanitized Filenames in Auto-Generated HTML
**Vulnerability:** XSS via Malicious File/Directory Names
**Learning:** Tools that auto-generate static HTML pages from local file systems often overlook input sanitization, implicitly trusting local file paths. If these generated pages are hosted or shared, an attacker can create files with names like `<script>alert(1)</script>` to execute arbitrary JavaScript in the context of the user viewing the generated index.
**Prevention:** Always HTML-encode variable data injected into HTML templates, and URL-encode data used in `href` attributes, regardless of the data's origin (even if it's "just" the local file system). Additionally, ensure HTML attributes like `href` are properly quoted to prevent attribute breakout.
## 2024-06-26 - [Directory Traversal via Symlink / Un-writable DoS]
**Vulnerability:** The application crawled symlink directories and crashed on directories without read permissions due to missing listFiles() null checks.
**Learning:** `File.listFiles()` returns null (not empty) on unreadable directories. Standard directory crawling must both skip symlinks to avoid traversal and explicitly handle null returns to avoid crashes.
**Prevention:** Use `java.nio.file.Files.isSymbolicLink(file.toPath())` when iterating directories to avoid symlink traversal, and gracefully handle `null` arrays from `listFiles()` and `list()`.
