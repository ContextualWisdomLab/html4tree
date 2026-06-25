## 2024-06-25 - [Add Semantic Navigation for Accessibility]
**Learning:** Adding explicit ARIA roles (e.g., `role="navigation"`) and `aria-label` to dynamically generated directory trees improves navigation for screen reader users by distinguishing file listings from regular content. Directory listings inherently function as navigation menus.
**Action:** Always wrap directory tree `<ul>` tags in a `<nav>` tag and add an appropriate `aria-label` when dynamically generating index files to ensure screen readers announce it as a distinct navigation region.
