# Generated navigation landmark label

## Decision

Generated directory pages keep one HTML `nav` landmark for the directory-link list. The landmark now references an in-document `<h2>` through `aria-labelledby`. The heading uses the existing `visually-hidden` utility, so it remains semantic content without changing the visible layout.

This is an accessibility structure choice, not a claim that `aria-label` is invalid. W3C WAI-ARIA Authoring Practices states that a landmark which begins with a heading can use that heading as its label through `aria-labelledby`; it also notes that a single navigation landmark does not require a label. The Navigation Landmark example likewise demonstrates a `nav` containing an `h2`. The generated page retains the explicit label because "디렉토리 목록" is useful descriptive context and the same text can participate in heading navigation.

## Constraints and alternatives

The previous `aria-label="디렉토리 목록"` was a valid accessible-name mechanism. Keeping it would have been the smallest implementation, but it would not add a heading target. A visible `<h2>` would expose the same structure to all users but would change the established visual composition. An unlabeled single `nav` is standards-compatible but removes the existing descriptive landmark name. The selected hidden-heading approach preserves the current visual layout while adding heading semantics.

The implementation deliberately does not claim universal browser-translation behavior. Browser translation engines and assistive technologies are external consumers whose behavior can vary by version and configuration. Source inspection and string-level regressions prove the generated DOM relationship only; they do not prove translation, screen-reader announcement order, or cross-browser accessibility behavior.

## Verification contract

The generated page must contain exactly one `id="nav-heading"`, exactly one `aria-labelledby="nav-heading"`, and the referenced hidden heading text. The `nav` must not also carry a competing `aria-label`. Current-head browser/accessibility-tree evidence is required before treating the change as fully verified UI behavior. Locale coverage beyond the generated Korean interface is not inferred from this structural change.

Rollback is local: restore the previous `nav aria-label` and remove the hidden heading and its focused regression if browser or assistive-technology evidence shows a regression. The broader directory-list semantics, keyboard focus, link targets, CSP, and hidden-text utility remain independent.

## Traceability

World Wide Web Consortium, Web Accessibility Initiative. (n.d.). *Landmark regions*. WAI-ARIA Authoring Practices Guide. Retrieved September 5, 2026, from https://www.w3.org/WAI/ARIA/apg/practices/landmark-regions/

World Wide Web Consortium, Web Accessibility Initiative. (n.d.). *Navigation landmark: ARIA landmark example*. WAI-ARIA Authoring Practices Guide. Retrieved September 5, 2026, from https://www.w3.org/WAI/ARIA/apg/patterns/landmarks/examples/navigation.html
