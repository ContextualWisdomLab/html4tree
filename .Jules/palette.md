## 2024-05-24 - Screen Reader Compatibility for HTML Generators
**Learning:** Raw Unicode icons in automatically generated HTML directory listings can cause noisy screen reader experiences. Adding simple `<span aria-hidden="true">` wrappers dramatically improves accessibility.
**Action:** When working on template engines or string-based HTML generators, always identify decorative content (like Unicode icons) and ensure they are explicitly hidden from assistive technology.
