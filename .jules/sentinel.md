## 2024-06-21 - [html4tree] Unsanitized Filenames in Auto-Generated HTML
**Vulnerability:** XSS via Malicious File/Directory Names
**Learning:** Tools that auto-generate static HTML pages from local file systems often overlook input sanitization, implicitly trusting local file paths. If these generated pages are hosted or shared, an attacker can create files with names like `<script>alert(1)</script>` to execute arbitrary JavaScript in the context of the user viewing the generated index.
**Prevention:** Always HTML-encode variable data injected into HTML templates, and URL-encode data used in `href` attributes, regardless of the data's origin (even if it's "just" the local file system). Additionally, ensure HTML attributes like `href` are properly quoted to prevent attribute breakout.

## 2026-06-25 - [Add Content-Security-Policy and UTF-8 Charset]
**Vulnerability:** The generated HTML site lacked a strict Content-Security-Policy (CSP) and an explicit charset mapping. This could lead to XSS attacks or UTF-7 downgrade exploits on static HTML files.
**Learning:** For a command-line tool generating static HTML elements, it is essential to defensively encode output via headers/meta tags within the HTML file itself since we cannot rely on a traditional web server to enforce these headers.
**Prevention:** Always enforce a restrictive CSP (`default-src 'none'; style-src 'unsafe-inline';`) and declare `<meta charset="utf-8">` in generated HTML templates to prevent content injection or downgrade vectors.
