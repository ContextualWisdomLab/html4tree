# Generated index readability, bidirectionality, and focus treatment

## Decision

The generated `index.html` page is html4tree's primary user-facing artifact. Its embedded stylesheet therefore applies the following bounded presentation contract without changing file discovery, ordering, escaping, URLs, filesystem access, or the Content Security Policy mechanism:

- adjacent list rows are separated with `li + li`, so the first row has no unnecessary leading line and no special last-row override is required;
- the empty-directory status uses explicit foreground colors in light and dark color schemes instead of inherited color plus opacity;
- hover and keyboard-focus states underline only the visible `.entry-name`, while the existing two-CSS-pixel outline continues to surround the complete interactive target;
- user-controlled file and directory names use `dir="auto"` together with `unicode-bidi: isolate`, while translatable file-type text remains a separate visually hidden sibling for the accessible name;
- mixed-direction tooltip text wraps the user-controlled name in Unicode FIRST STRONG ISOLATE (U+2068) and POP DIRECTIONAL ISOLATE (U+2069), preventing filename directionality from reordering the translated type suffix;
- the fixed parent-directory marker `..` does not request automatic directionality because it contains no strong directional character;
- reduced-motion behavior remains unchanged; and
- dark-mode overrides appear after their corresponding base declarations so the cascade is deterministic.

The empty-directory information icon remains decorative, is hidden from assistive technology with `aria-hidden="true"`, and stays inside the existing `role="status"` container. No new emoji is selected solely for visual appearance.

## Bidirectional text engineering basis

The HTML Standard defines `dir="auto"` as directionally isolated text whose direction is inferred from its first strong directional character. Unicode Standard Annex #9 defines directional isolates so that text inside the isolate does not influence ordering outside it and vice versa, and maps HTML `dir="auto"` to the same isolation model. The generated index therefore treats each buyer-controlled filename as one isolated unit instead of allowing Hebrew, Arabic, Latin, digits, punctuation, or neutral characters in the filename to reorder adjacent interface text.

For visible HTML, the implementation uses markup and CSS rather than embedding Unicode controls into the displayed filename: the filename span carries `dir="auto"` and `.entry-name { unicode-bidi: isolate; }`. For the plain-text `title` attribute, which cannot contain nested markup, the implementation uses FSI/PDI around only the filename and leaves the translated type suffix outside that isolate. The accessible link name remains composed from the visible filename plus the existing visually hidden type label; an `aria-label` is deliberately not introduced because doing so would replace that translatable text contract.

## Sensitive-name normalization boundary

Security matching may normalize an observed filesystem name to decide whether it is sensitive, but the exclusion set must retain the exact observed filesystem identity. A leading or trailing space can be a real filename character. Storing only the trimmed spelling (for example, matching ` .git` as `.git` and then excluding only `.git`) allows the original padded entry to survive the later exact-name exclusion check and become buyer-visible. Regression coverage therefore verifies that normalization is used only for policy comparison while the exact original name is stored in the exclusion set.

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
2. mixed-direction buyer-controlled entry names use the isolated visible-name contract without replacing the translatable accessible text;
3. tooltip text isolates the filename from the translated type suffix;
4. an empty directory emits exactly one semantic status row;
5. adjacent-row separators do not rely on a trailing-border exception;
6. empty-state text uses explicit light and dark colors and no opacity declaration;
7. dark-mode declarations follow their base declarations;
8. hover and `:focus-visible` underline only the visible entry name while preserving the full-target outline;
9. reduced-motion styling remains present; and
10. the authored text and focus colors meet the documented numeric thresholds.

`SensitiveWhitespaceRegressionTest` verifies that a whitespace-padded sensitive filename is normalized for policy matching but excluded under its exact filesystem identity, while an ordinary whitespace-padded filename remains visible.

`CspHashTest` independently recomputes the digest from the exact emitted `<style>` bytes. Any CSS change must continue to preserve the single-source `CSS_CONTENT` and `STYLE_HASH` byte-identity contract rather than weakening CSP with `unsafe-inline`, a broader source expression, or a duplicated stylesheet fixture.

## References

The Unicode Consortium. (2025, August 13). *Unicode bidirectional algorithm* (Unicode Standard Annex #9, Version 17.0.0, Revision 51). https://www.unicode.org/reports/tr9/

WHATWG. (2026). *HTML Standard: The `dir` attribute*. Retrieved August 28, 2026, from https://html.spec.whatwg.org/multipage/dom.html#the-dir-attribute

World Wide Web Consortium. (2024, December 12). *Web Content Accessibility Guidelines (WCAG) 2.2* (W3C Recommendation). https://www.w3.org/TR/WCAG22/

World Wide Web Consortium. (2026, February 11). *Understanding WCAG 2.2*. Web Accessibility Initiative. https://www.w3.org/WAI/WCAG22/Understanding/

World Wide Web Consortium. (2026, March 9). *Understanding Success Criterion 2.4.13: Focus appearance*. Web Accessibility Initiative. https://www.w3.org/WAI/WCAG22/Understanding/focus-appearance.html
