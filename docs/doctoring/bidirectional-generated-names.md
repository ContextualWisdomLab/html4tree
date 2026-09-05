# Bidirectional direction for generated names

## Problem and scope

`html4tree` emits directory and file names supplied by the filesystem into a Korean generated index. Their base direction is not known in advance. A fixed page direction is therefore not a reliable direction source for Arabic, Hebrew, or mixed-script names.

The product boundary is deliberately narrow: apply HTML `dir="auto"` to the dynamic directory heading and visible filename span only. Fixed product copy such as the parent-navigation `..` token, icon semantics, file/directory type labels, and empty-state copy keep their existing page semantics. This decision does not claim that `dir="auto"` solves every punctuation or mixed-direction corner case.

## Decision

Use `dir="auto"` on the elements that tightly wrap the externally supplied directory and filename strings. Preserve the semantic `.entry-name` target used by the interaction styling contract; directionality must not regress that markup into a positional selector.

The WHATWG HTML Standard defines the `auto` state as directionally isolated text whose direction is determined from element content, using a first-strong-character heuristic. W3C Internationalization likewise recommends `dir="auto"` when inserted text can be multilingual and its direction is not known in advance, while noting that corner cases remain.

## Verification

`BidirectionalTextDirectionTest` generates a real index for mixed Arabic/Latin directory and filename text and requires:

- `dir="auto"` on the dynamic `<h1>`;
- `dir="auto"` on the visible `.entry-name` filename span;
- no `dir="auto"` on the fixed parent-navigation row;
- no `dir="auto"` on the fixed visually hidden file-type label.

The existing generated-index readability regression remains authoritative for `.entry-name` hover, focus-visible, and active styling. Browser rendering and accessibility-tree evidence on the exact head remain promotion evidence rather than being inferred from source inspection.

## Alternatives and rollback

A document-wide `dir="rtl"` or `dir="auto"` was rejected because the page chrome and localized Korean copy have known direction. CSS-only direction control was rejected because HTML direction metadata remains meaningful when CSS is unavailable. Unicode directional control characters were rejected because the dynamic text already has an element boundary and invisible control state would make the contract harder to audit.

Rollback is the removal of the two `dir="auto"` attributes and this focused regression if current browser evidence shows a product regression. The `.entry-name` semantic interaction target is independent and must remain intact.

## References

WHATWG. (2026). *HTML Standard: The `dir` attribute*. https://html.spec.whatwg.org/multipage/dom.html#the-dir-attribute

W3C Internationalization. (n.d.). *Structural markup and right-to-left text in HTML*. https://www.w3.org/International/questions/qa-html-dir
