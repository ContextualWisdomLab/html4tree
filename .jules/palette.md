
## 2024-05-18 - Improve Screen Reader Experience for Directory Trees
**Learning:** Decorative unicode icons (like &#128193; for folders or &#x21B0; for back arrows) in generated HTML directory listings are read aloud by screen readers as confusing literals, degrading the user experience.
**Action:** Wrap decorative emoji and unicode symbols in `<span aria-hidden="true">` to prevent screen readers from announcing them, and add explicit `aria-label` attributes to ambiguous links (like `..`) to provide context.
