## 2024-05-23 - XSS in Static HTML Generation
**Vulnerability:** The application generates `index.html` files by directly concatenating directory and file names without any escaping or sanitization.
**Learning:** File systems allow characters like `<` and `>` in file names, which can lead to Stored XSS when these names are rendered in generated HTML directory listings. Furthermore, `href` attributes were unquoted and unencoded, leading to broken links and potential injection.
**Prevention:** Always HTML-escape file/directory names before rendering them in HTML text content, and URL-encode them and wrap them in quotes when used in `href` attributes.
