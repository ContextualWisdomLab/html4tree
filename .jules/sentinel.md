## 2024-06-23 - [XSS in Directory Indexing Generator]
**Vulnerability:** Found an XSS vulnerability where HTML files generated from a directory index unescaped file names. If a malicious file had `<script>` tags in its name, it would run arbitrary JavaScript in the index page.
**Learning:** Even static site generators are vulnerable to XSS if inputs (like filenames) are interpreted by the browser.
**Prevention:** Always escape HTML special characters when inserting untrusted input into HTML context, and URL encode input for URLs/href attributes.