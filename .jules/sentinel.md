## 2024-05-18 - XSS in HTML Generator
**Vulnerability:** XSS via unescaped filenames and unquoted href attributes in the generated `index.html`.
**Learning:** The application generated HTML by concatenating raw filenames, leading to an XSS risk. While testing the fix locally using maliciously named files works, committing test files containing characters like `<`, `>`, and `"` breaks `git clone` for developers on Windows.
**Prevention:** Always escape text and encode URLs when generating HTML manually. For XSS testing, generate malicious test files dynamically in code during the test suite execution (e.g., in a temporary directory) instead of hardcoding them into the repository.
