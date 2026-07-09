## 2024-07-09 - HTML Directory Listing A11y
**Learning:** Pure HTML output files need semantic HTML elements like `<main>`, `<nav>`, `<ul>` and `<li>`, and CSS-driven interaction hints (hover, focus-visible) are crucial for accessibility and UX in static site generation. Screen readers and keyboard navigation rely heavily on these base HTML tags when JS is not present.
**Action:** When working on CLI tools that output HTML, treat the generated HTML string as a full web page interface, applying standard web accessibility practices.

## 2024-07-09 - CLI HTML Typography and Dark Mode
**Learning:** Even statically generated directory listings need proper typography and color scheme support. Without a constrained max-width, line length becomes unreadable on large monitors. Without dark mode support (`prefers-color-scheme: dark`), opening the generated HTML page can cause sudden visual strain for users on dark-themed OS settings.
**Action:** Always include basic typography constraints (max-width, line-height, system font) and dark mode media queries for any CLI tool that generates HTML output to ensure accessibility and visual comfort.
