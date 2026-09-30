# Product technical gap baseline

Status: Proposed
Pull request: #826
Reviewed source head: `178123349feca53fe2cd9c82f893ed9df179838c`
DOM-contract repair head: `757a05c2b883c92dc29d70df1712dcaef5f7d1df`

## Goal and ownership

html4tree is the canonical writer for the static directory-index HTML, inline CSS, and generated interaction semantics. The generated presentation describes filesystem entries but does not replace filesystem truth. This PR changes the buyer-visible hover and keyboard-focus feedback for generated file and directory links.

## Root cause and repair

The product CSS targets the visible label with `span:nth-child(2)`, because the final span is assistive text. The original regression asserted only the CSS selector while generating an empty directory, so DOM child order could change without failing the test. The focused contract now generates a real file link, verifies that the visible label remains the second span between the decorative icon and visually hidden type text, and retains the CSS selector assertion.

## Exact-head acceptance matrix

| Capability | Current evidence | Status | Required action |
| --- | --- | --- | --- |
| Determinism | Generated-file contract binds emitted DOM and inline CSS | REPAIRED, NOT REVALIDATED | Require fresh exact-head CI |
| Semantics | Decorative icon is hidden; file type remains assistive text | GREEN — source contract | Verify accessibility tree in a real browser |
| Keyboard/pointer | Full link retains focus outline; visible label receives underline | GREEN — source contract | Exercise pointer and keyboard on current head |
| Touch | Link padding exists; measured target size is absent | FAIL | Verify 44×44 CSS-pixel target where applicable |
| Responsive/zoom | Viewport metadata and wrapping exist | NOT REVALIDATED | Capture mobile, intermediate, desktop, 200% zoom/reflow |
| Reduced motion | Transition removal contract exists | GREEN — source contract | Verify browser `prefers-reduced-motion` behavior |
| Loading/error/offline/permission/read-only/stale/conflict/retry/busy | Static generated page has no runtime API state | NOT APPLICABLE | Reclassify if interactive loading is introduced |
| Locales | Generated copy is Korean-only | FAIL | Define and validate ko/en/ja/zh/vi/es/de/fr resource ownership |
| Large data/performance | Sorted generation exists; browser rendering evidence absent | FAIL | Measure large-directory generation and rendering |
| Import/export/recovery | Atomic write fallback is outside this focused delta | NOT REVALIDATED | Preserve rollback and interrupted-write tests |

## Merge gate

Keep Draft until the new exact head has terminal required Checks and qualifying review, plus current-head real-browser keyboard, pointer, touch, responsive, zoom/reflow, reduced-motion, accessibility, locale, and realistic large-directory evidence. Skipped CodeQL or source inspection alone is not acceptance.
