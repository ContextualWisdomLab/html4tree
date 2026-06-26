## 2024-06-26 - [Add Accessibility Meta Tags & Focus States]
**Learning:** The generated index.html directory trees were completely lacking basic accessibility metadata (`lang`, viewport, charset) and critical keyboard focus indicators for the links, rendering them unnavigable for screen readers or keyboard-only users.
**Action:** Always inject `lang`, charset, viewport meta tags, and `:focus` styles into raw, generated HTML string templates to ensure baseline accessibility compliance.
