# Generated index readability and focus treatment

## Decision

The generated `index.html` page is html4tree's primary user-facing artifact. Its embedded stylesheet and markup therefore apply the following bounded presentation contract without changing file discovery, ordering, escaping, URLs, filesystem access, or the Content Security Policy mechanism:

- adjacent list rows are separated with `li + li`, so the first row has no unnecessary leading line and no special last-row override is required;
- the empty-directory message uses explicit foreground colors in light and dark color schemes instead of inherited color plus opacity;
- the build-time empty-directory message remains ordinary static list content and does not use the WAI-ARIA `status` live-region role;
- hover and keyboard-focus states underline only the link's textual span, while the existing two-CSS-pixel outline continues to surround the complete interactive target;
- reduced-motion behavior remains unchanged; and
- dark-mode overrides appear after their corresponding base declarations so the cascade is deterministic.

The empty-directory folder icon remains decorative and is hidden from assistive technology with `aria-hidden="true"`. The surrounding `.empty-dir` element carries no live-region role because its text is generated as part of the initial document rather than being inserted or updated as an advisory status after the interface is already present.

## Accessibility engineering basis

WAI-ARIA 1.2 defines `status` as a live-region role for advisory information and gives it implicit `aria-live="polite"` and `aria-atomic="true"` semantics. WCAG 2.2 Success Criterion 4.1.3 requires actual status messages to be programmatically determinable so assistive technologies can present them without moving focus. That criterion does not require every static informational sentence in the initially generated document to be exposed as a live region. For html4tree's build-time empty-directory row, ordinary semantic document content is the narrower and more truthful contract; a future dynamically updated empty state would need a separate status-message analysis.

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
2. an empty directory emits exactly one ordinary empty-state row and does not assign that row `role="status"`;
3. adjacent-row separators do not rely on a trailing-border exception;
4. empty-state text uses explicit light and dark colors and no opacity declaration;
5. dark-mode declarations follow their base declarations;
6. hover and `:focus-visible` underline only the textual span while preserving the full-target outline;
7. reduced-motion styling remains present; and
8. the authored text and focus colors meet the documented numeric thresholds.

`MainTest` separately requires the generated empty-directory document to contain the buyer-visible empty-state copy while omitting `role="status"`. This locks the static-content decision at the complete generated-page boundary rather than relying on a source-string-only assertion.

`CspHashTest` independently recomputes the digest from the exact emitted `<style>` bytes. Any CSS change must continue to preserve the single-source `CSS_CONTENT` and `STYLE_HASH` byte-identity contract rather than weakening CSP with `unsafe-inline`, a broader source expression, or a duplicated stylesheet fixture.

## Claim boundary and rollback

Removing `role="status"` does not claim that every screen reader would otherwise announce the initial content twice, nor does it claim whole-product accessibility conformance. Assistive-technology treatment of live regions can vary. The implemented claim is narrower: the generated empty-directory message is static document content, while `status` carries live-region semantics intended for status information and updates.

If html4tree later introduces client-side directory refresh or another operation that changes the empty-state message after initial load, revisit the status-message contract against the then-current WAI-ARIA and WCAG requirements rather than restoring a live-region role by default.

## References

World Wide Web Consortium. (2023, June 6). *Accessible Rich Internet Applications (WAI-ARIA) 1.2* (W3C Recommendation). https://www.w3.org/TR/2023/REC-wai-aria-1.2-20230606/

World Wide Web Consortium. (2024, December 12). *Web Content Accessibility Guidelines (WCAG) 2.2* (W3C Recommendation). https://www.w3.org/TR/2024/REC-WCAG22-20241212/

World Wide Web Consortium. (2026, February 11). *Understanding WCAG 2.2*. Web Accessibility Initiative. https://www.w3.org/WAI/WCAG22/Understanding/

World Wide Web Consortium. (2026, March 9). *Understanding Success Criterion 2.4.13: Focus appearance*. Web Accessibility Initiative. https://www.w3.org/WAI/WCAG22/Understanding/focus-appearance.html
