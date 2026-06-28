## 2024-06-21 - [html4tree] Unsanitized Filenames in Auto-Generated HTML
**Vulnerability:** XSS via Malicious File/Directory Names
**Learning:** Tools that auto-generate static HTML pages from local file systems often overlook input sanitization, implicitly trusting local file paths. If these generated pages are hosted or shared, an attacker can create files with names like `<script>alert(1)</script>` to execute arbitrary JavaScript in the context of the user viewing the generated index.
**Prevention:** Always HTML-encode variable data injected into HTML templates, and URL-encode data used in `href` attributes, regardless of the data's origin (even if it's "just" the local file system). Additionally, ensure HTML attributes like `href` are properly quoted to prevent attribute breakout.

## 2024-06-25 - [html4tree] Arbitrary File Overwrite via Symlinks
**Vulnerability:** Arbitrary file overwrite / Symlink attack in generated `index.html` files.
**Learning:** Static site generators and scripts that write files blindly to local directories are susceptible to symlink attacks. If an attacker controls a directory being crawled and places a symlink named `index.html` pointing to a sensitive file (e.g., `/etc/passwd` or an internal configuration file), writing to `File(curr_dir, "index.html")` will follow the symlink and overwrite the target file with the generated HTML output.
**Prevention:** Before writing to output files, check if the file already exists as a symbolic link (`java.nio.file.Files.isSymbolicLink(file.toPath())`) and handle it securely, such as by explicitly deleting the symlink before writing the new content or refusing to write to the file.
