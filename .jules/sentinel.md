## 2024-06-22 - XSS and Path Injection in HTML Generation
**Vulnerability:** Unescaped file and directory names are directly interpolated into the generated index.html content (title, h1, and href attributes without quotes), allowing Cross-Site Scripting (XSS).
**Learning:** Building HTML structures through manual string concatenation without escaping leads to missing proper encoding for user-controlled input, in this case, the file system entries.
**Prevention:** Always use proper HTML encoding for text content and URL encoding for link attributes. Surround attributes with quotes.
