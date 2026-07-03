## 2024-07-02 - [CSS Hover Transitions for Accessibility]
**Learning:** For hover/focus states, transition only specific properties like `background-color` and `outline-color` rather than using `transition: all` to prevent unintended animations and performance issues. Always include `@media (prefers-reduced-motion: reduce)` override to respect user accessibility preferences.
**Action:** Always include simple CSS transitions on specific properties for interactive elements like links and buttons when building dynamic HTML generation utilities, and always provide a reduced-motion fallback.
