## 2024-06-21 - [html4tree] Unsanitized Filenames in Auto-Generated HTML
**Vulnerability:** XSS via Malicious File/Directory Names
**Learning:** Tools that auto-generate static HTML pages from local file systems often overlook input sanitization, implicitly trusting local file paths. If these generated pages are hosted or shared, an attacker can create files with names like `<script>alert(1)</script>` to execute arbitrary JavaScript in the context of the user viewing the generated index.
**Prevention:** Always HTML-encode variable data injected into HTML templates, and URL-encode data used in `href` attributes, regardless of the data's origin (even if it's "just" the local file system). Additionally, ensure HTML attributes like `href` are properly quoted to prevent attribute breakout.
## 2023-10-25 - Path Traversal via Symlinks in Directory Crawlers
**Vulnerability:** The `html4tree` crawler was following symbolic links when generating its static HTML index, creating potential path traversal and arbitrary directory read vulnerabilities if links pointed outside the scope of the tree.
**Learning:** Checking `isDirectory()` is not enough in recursive tree walkers. In Kotlin/Java, a directory pointing to another part of the filesystem via symbolic link will pass `isDirectory()` checks but bypass the intended boundaries.
**Prevention:** Always check `!java.nio.file.Files.isSymbolicLink(file.toPath())` along with directory checks when traversing the file system recursively unless intentionally supporting external links with validation.
