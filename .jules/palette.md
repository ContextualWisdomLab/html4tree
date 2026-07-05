## 2024-05-15 - Enhancing Generated File Tree HTML
**Learning:** Raw directory listings generated into HTML often lack critical accessibility meta tags (like `lang="en"`, `viewport` for responsive scaling) and aria-labels for directory/file links (which are otherwise non-descriptive for screen readers like just `&#128193; folderName`).
**Action:** Always include base semantic structures (`<main>`, `<html lang="en">`, responsive meta tags) and aria-labels specifying the entity type (e.g., `folderName directory`) when generating raw navigation trees to ensure keyboard and screen-reader usability. Add `:focus` styles mimicking `:hover` for keyboard users.

## 2024-05-18 - Improve Screen Reader Experience for Directory Trees
**Learning:** Decorative unicode icons (like &#128193; for folders or &#x21B0; for back arrows) in generated HTML directory listings are read aloud by screen readers as confusing literals, degrading the user experience.
**Action:** Wrap decorative emoji and unicode symbols in `<span aria-hidden="true">` to prevent screen readers from announcing them, and add explicit `aria-label` attributes to ambiguous links (like `..`) to provide context.

## 2024-05-24 - Providing Helpful Empty States for Generated HTML Trees
**Learning:** Automatically generated directory listings often fail to provide guidance when a directory contains no viewable files, leaving users with an empty list structure that looks broken or confusing.
**Action:** When generating raw navigation trees, always check if the content list is empty. If it is, inject a visually distinct, helpful message (e.g. italicized gray text) explaining that the directory is empty, rather than rendering an empty `<ul>`.

## 2024-06-25 - Directory Listing Navigation Landmark
**Learning:** Generated directory listings act as navigation regions, and screen readers benefit when the listing is announced separately from the page's main content.
**Action:** Wrap generated directory listing `<ul>` elements in `<nav aria-label="Directory listing">` while keeping the surrounding semantic `<main>` structure.
## 2026-07-05 - Enhance screen reader support for semantic lists
**Learning:** Screen readers like VoiceOver sometimes strip list semantics from `<ul>` when `list-style: none` is applied, reducing accessibility for users navigating directory trees.
**Action:** Add `role="list"` explicitly to `<ul>` elements when their default list style is removed in CSS to ensure screen readers announce them properly.
