## 2024-06-21 - [html4tree] Unsanitized Filenames in Auto-Generated HTML
**Vulnerability:** XSS via Malicious File/Directory Names
**Learning:** Tools that auto-generate static HTML pages from local file systems often overlook input sanitization, implicitly trusting local file paths. If these generated pages are hosted or shared, an attacker can create files with names like `<script>alert(1)</script>` to execute arbitrary JavaScript in the context of the user viewing the generated index.
**Prevention:** Always HTML-encode variable data injected into HTML templates, and URL-encode data used in `href` attributes, regardless of the data's origin (even if it's "just" the local file system). Additionally, ensure HTML attributes like `href` are properly quoted to prevent attribute breakout.
## 2024-06-28 - [html4tree] Static HTML Generation Security
**Vulnerability:** Defense in Depth (CSP Missing)
**Learning:** Even when inputs are properly escaped, statically generated HTML that displays file/directory structures should implement a Content Security Policy (CSP) to provide an extra layer of defense against potential XSS bypasses.
**Prevention:** Include a strict CSP meta tag (e.g., `default-src 'none'; style-src 'unsafe-inline';`) in auto-generated HTML headers when external scripts or resources are not required.
