## 2024-05-15 - Enhancing Generated File Tree HTML
**Learning:** Raw directory listings generated into HTML often lack critical accessibility meta tags (like `lang="en"`, `viewport` for responsive scaling) and aria-labels for directory/file links (which are otherwise non-descriptive for screen readers like just `&#128193; folderName`).
**Action:** Always include base semantic structures (`<main>`, `<html lang="en">`, responsive meta tags) and `aria-label`s specifying the entity type (e.g., `folderName directory`) when generating raw navigation trees to ensure keyboard and screen-reader usability. Add `:focus` styles mimicking `:hover` for keyboard users.

## 2024-06-25 - Directory Listing Navigation Landmark
**Learning:** Generated directory listings act as navigation regions, and screen readers benefit when the listing is announced separately from the page's main content.
**Action:** Wrap generated directory listing `<ul>` elements in `<nav aria-label="Directory listing">` while keeping the surrounding semantic `<main>` structure.
