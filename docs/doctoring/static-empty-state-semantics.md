# Static empty-state semantics

- Date: 2026-08-14
- Scope: the empty-directory message rendered in the initial HTML document

## Decision

The empty-directory message remains ordinary document content inside the existing list and navigation structure. It does not use `role="status"`.

The WAI-ARIA `status` role is a live-region role. It carries implicit `aria-live="polite"` and `aria-atomic="true"` behavior for advisory information whose content changes. The html4tree empty state is present when the static document first loads and is not updated by client-side interaction. Assigning live-region semantics adds a change-announcement contract that the page does not need.

This change does not remove the visible message, the decorative icon's `aria-hidden="true"`, the list semantics, or the navigation landmark.

## Verification contract

- The empty-directory message is rendered exactly once.
- The generated document does not contain `role="status"` for this static state.
- The list and navigation semantics remain present.
- The visible text remains available in the ordinary accessibility tree.

## References

World Wide Web Consortium. (2023). *Accessible Rich Internet Applications (WAI-ARIA) 1.2*. https://www.w3.org/TR/wai-aria-1.2/

World Wide Web Consortium. (2026). *Accessible Rich Internet Applications (WAI-ARIA) 1.3*. https://www.w3.org/TR/wai-aria-1.3/
