# Product technical gap baseline

Status: Proposed  
Predecessor pull request: [#826](https://github.com/ContextualWisdomLab/html4tree/pull/826)  
Predecessor regression head: `545b6267d41402d3affc8c2985436745ff00cdb5`  
Preserved review-repair head: `757a05c2b883c92dc29d70df1712dcaef5f7d1df`  
Successor DOM-contract repair head: `2faabca012908c6fc7a0f7410a2a3b437ea0d97a`

## Goal and ownership

html4tree is the canonical writer for static directory-index HTML, inline CSS,
and generated interaction semantics. The presentation describes filesystem
entries; it does not replace filesystem domain truth.

## Root cause and repair

The predecessor test asserted the `span:nth-child(2)` selector while generating
an empty directory, so it did not protect the generated link child order that
makes the second span the visible label. The direct repair was removed by the
concurrent predecessor head `545b6267d41402d3affc8c2985436745ff00cdb5`.
This successor starts from that exact head and restores a real generated-file
DOM contract without changing the production CSS.

## Exact-head acceptance matrix

| Area | Exact evidence | Status | Merge gate |
| --- | --- | --- | --- |
| Determinism | Generated file-link DOM and CSS selector are asserted together at `2faabca012908c6fc7a0f7410a2a3b437ea0d97a` | Revalidation pending | Exact-head CI and review must pass |
| Semantics | Source contract preserves decorative icon, visible label, and visually hidden type text | Source-only PASS | Confirm in a real browser and accessibility tree |
| Pointer and keyboard | Source covers hover and `:focus-visible`; no current-head browser recording | PARTIAL | Pointer and keyboard interaction evidence |
| Touch | Padding exists; measured 44 by 44 CSS-pixel target evidence is absent | FAIL | Mobile touch measurement and interaction |
| Responsive and zoom | No current-head desktop, intermediate, mobile, or 200% zoom screenshots | FAIL | Capture all required viewports and zoom |
| Reduced motion | Source contains a reduced-motion media query | Source-only PASS | Browser verification with emulation |
| Runtime states | Static generated index has no loading, offline mutation, permission, conflict, retry, or busy transitions | Not applicable | Keep this boundary explicit |
| Locales | Generated user-facing strings are Korean-only | FAIL | Product decision and coverage for ko/en/ja/zh/vi/es/de/fr |
| Large data and performance | No current-head large-directory measurement | FAIL | Measure realistic large-directory generation and rendering |
| Import, export, recovery | Filesystem-to-static-HTML generation exists; current-head recovery and rollback evidence is absent | FAIL | Verify deterministic regeneration and rollback |

A skipped CodeQL job is not acceptance evidence. This work remains Draft until
the applicable exact-head checks, review, browser, accessibility, locale, and
large-directory gates are satisfied.
