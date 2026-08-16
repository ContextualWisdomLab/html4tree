# Generated index readability and focus treatment

## Decision

The generated `index.html` page is html4tree's primary user-facing artifact. Its embedded stylesheet therefore applies the following bounded presentation contract without changing file discovery, ordering, escaping, URLs, filesystem access, or the Content Security Policy mechanism:

- adjacent list rows are separated with `li + li`, so the first row has no unnecessary leading line and no special last-row override is required;
- the empty-directory status uses explicit foreground colors in light and dark color schemes instead of inherited color plus opacity;
- hover and keyboard-focus states underline `.entry-name` (the visible filename), while the existing two-CSS-pixel outline continues to surround the complete interactive target;
- reduced-motion behavior remains unchanged; and
- dark-mode overrides appear after their corresponding base declarations so the cascade is deterministic; and
- size and last-modified metadata use `--listing-meta` / `--listing-dark-meta`, the same contrast-tested pair as empty-state text.

The empty-directory information icon remains decorative, is hidden from assistive technology with `aria-hidden="true"`, and stays inside the existing `role="status"` container. No new emoji is selected solely for visual appearance.

## Accessibility engineering basis

WCAG 2.2 Success Criterion 1.4.3 requires at least 4.5:1 contrast for ordinary text. Success Criterion 2.4.13, which is Level AAA, describes a keyboard focus indicator area at least as large as a two-CSS-pixel perimeter and a focused-versus-unfocused contrast change of at least 3:1. The W3C Understanding documents explain these criteria but are informative rather than normative.

html4tree retains a solid two-CSS-pixel outline around the full link. The engineering tests calculate the following sRGB relative-luminance ratios from the authored CSS colors:

| Surface | Foreground or indicator | Background | Calculated ratio | Engineering threshold |
| --- | --- | --- | ---: | ---: |
| Empty-state text, light | `#656d76` | `#ffffff` | 5.2469:1 | 4.5:1 |
| Empty-state text, dark | `#8b949e` | `#0d1117` | 6.1527:1 | 4.5:1 |
| Focus outline, light hover/focus surface | `#0969da` | `#f6f8fa` | 4.8771:1 | 3:1 |
| Focus outline, dark hover/focus surface | `#58a6ff` | `#161b22` | 6.8480:1 | 3:1 |

These deterministic source-level checks are regression evidence, not a declaration of formal WCAG conformance. Browser rendering, anti-aliasing, zoom, forced colors, operating-system settings, extensions, and assistive-technology behavior still require representative manual and browser-based evaluation. The subtle row separator is a readability aid; this decision does not claim that the separator itself satisfies every possible WCAG non-text-contrast interpretation.

## Verification contract

`GeneratedIndexReadabilityTest` exercises the real generated page and verifies:

1. parent, first, middle, and last rows preserve the established output order;
2. an empty directory emits exactly one semantic status row;
3. adjacent-row separators do not rely on a trailing-border exception;
4. empty-state text uses explicit light and dark colors and no opacity declaration;
5. dark-mode declarations follow their base declarations;
6. hover and `:focus-visible` underline `.entry-name` while preserving the full-target outline;
7. reduced-motion styling remains present; and
8. the authored text and focus colors meet the documented numeric thresholds.

`CspHashTest` independently recomputes the digest from the exact emitted `<style>` bytes. Any CSS change must continue to preserve the single-source `CSS_CONTENT` and `STYLE_HASH` byte-identity contract rather than weakening CSP with `unsafe-inline`, a broader source expression, or a duplicated stylesheet fixture.

## References

World Wide Web Consortium. (2024, December 12). *Web Content Accessibility Guidelines (WCAG) 2.2* (W3C Recommendation). https://www.w3.org/TR/WCAG22/

World Wide Web Consortium. (2026, February 11). *Understanding WCAG 2.2*. Web Accessibility Initiative. https://www.w3.org/WAI/WCAG22/Understanding/

World Wide Web Consortium. (2026, March 9). *Understanding Success Criterion 2.4.13: Focus appearance*. Web Accessibility Initiative. https://www.w3.org/WAI/WCAG22/Understanding/focus-appearance.html
