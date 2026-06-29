## 2024-06-21 - [html4tree] Unsanitized Filenames in Auto-Generated HTML
**Vulnerability:** XSS via Malicious File/Directory Names
**Learning:** Tools that auto-generate static HTML pages from local file systems often overlook input sanitization, implicitly trusting local file paths. If these generated pages are hosted or shared, an attacker can create files with names like `<script>alert(1)</script>` to execute arbitrary JavaScript in the context of the user viewing the generated index.
**Prevention:** Always HTML-encode variable data injected into HTML templates, and URL-encode data used in `href` attributes, regardless of the data's origin (even if it's "just" the local file system). Additionally, ensure HTML attributes like `href` are properly quoted to prevent attribute breakout.

## 2024-06-29 - Symlink Attack: Overwriting Arbitrary Files
**Vulnerability:** The application created and wrote to `index.html` without first deleting any existing file or symbolic link by that name.
**Learning:** If an attacker can plant a symbolic link named `index.html` pointing to a sensitive file (e.g., `/etc/passwd` or `/home/user/.ssh/id_rsa`) in a directory that the application processes, running the application will overwrite the target of the symlink with the generated HTML content.
**Prevention:** Always use `java.nio.file.Files.deleteIfExists` or equivalent to safely remove any existing file or symbolic link before creating or overwriting a file in a user-controlled directory.
