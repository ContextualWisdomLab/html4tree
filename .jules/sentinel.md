## 2024-06-21 - [html4tree] Unsanitized Filenames in Auto-Generated HTML
**Vulnerability:** XSS via Malicious File/Directory Names
**Learning:** Tools that auto-generate static HTML pages from local file systems often overlook input sanitization, implicitly trusting local file paths. If these generated pages are hosted or shared, an attacker can create files with names like `<script>alert(1)</script>` to execute arbitrary JavaScript in the context of the user viewing the generated index.
**Prevention:** Always HTML-encode variable data injected into HTML templates, and URL-encode data used in `href` attributes, regardless of the data's origin (even if it's "just" the local file system). Additionally, ensure HTML attributes like `href` are properly quoted to prevent attribute breakout.
## 2023-10-27 - [Symlink Overwrite in file writes]
**Vulnerability:** Arbitrary file overwrite due to writing directly to `File(dir, "index.html").writeText(...)` which follows symlinks.
**Learning:** `File.writeText()` directly follows symlinks in Kotlin/Java. This allows symlink attacks if the directory structure can be controlled or if malicious symlinks are placed before execution.
**Prevention:** Explicitly check if the target path is a symlink using `java.nio.file.Files.isSymbolicLink(file.toPath())` and handle or delete it prior to writing file contents.
