# Crawl-root parent navigation

Status: **IMPLEMENTED**

## Decision

The CLI crawl omits the parent `..` row on the listing root and keeps it
on every nested directory. Direct `process_dir` callers still default to
showing `..`, because they are rendering one folder in isolation.

A published html4tree tree is a set of static files. `href="./.."` on the
root page resolves to the URL parent of that folder (WHATWG, n.d.;
Berners-Lee et al., 2005, §5.2.4). Apache `mod_autoindex` can still emit
"Parent Directory" because the HTTP server confines the URL space to the
document root (Apache Software Foundation, n.d.). html4tree has no such
server. A root `..` link therefore leaves the generated site, often to a
404 or to an unpublished parent.

WCAG 2.2 Success Criterion 2.4.4 requires that the purpose of a link be
determinable from its text and programmatically determined context
(World Wide Web Consortium, 2024). "상위 디렉토리로 이동" on the crawl
root names a destination that is not part of the generated set, so the
link purpose is false. Nested pages keep the same label because `..`
there stays inside the published tree.

Path identity uses `absoluteFile.toPath().normalize()`, not
`canonicalFile`, so a symlink root cannot be compared after the link is
followed.

## Buyer-visible contract

After `java -jar html4tree.jar <topdir>`, open the top `index.html`.
There must be no `href="./.."`. Open one nested folder's `index.html`
and use `..` to return to the parent listing. If the top page still
shows a parent row, regenerate from this release before publishing.

## Verification contract

`BuyerListingContractTest` runs `go()` on a tree that contains
`minutes.txt`, `scratch.tmp`, `.env`, `*.tmp`, `تقرير.pdf`, and
`invoices/note.txt`. It asserts keep/hide, isolation, `11 B`, UTC
`<time datetime>`, no root `..`, and a nested `..`.
`MainTest.testGoWithMaxLevel` also asserts the root omission.
`process_dir(..., includeParentLink = false)` covers the render flag
without a crawl.

## Rollback and recovery

Rollback restores the unconditional parent `<li>` in `process_dir`,
removes `listingRoot` from `crawl_directories`, removes
`same_normalized_path`, and updates this record plus `CHANGELOG.md`.
Nested navigation is unchanged either way.

## References

Apache Software Foundation. (n.d.). *Apache module mod_autoindex*.
https://httpd.apache.org/docs/2.4/mod/mod_autoindex.html

Berners-Lee, T., Fielding, R., & Masinter, L. (2005, January).
*Uniform Resource Identifier (URI): Generic syntax* (RFC 3986).
Internet Engineering Task Force.
https://www.rfc-editor.org/rfc/rfc3986

World Wide Web Consortium. (n.d.). *URL Standard*. WHATWG.
https://url.spec.whatwg.org/#path-relative-url-string

World Wide Web Consortium. (2024, December 12). *Web Content
Accessibility Guidelines (WCAG) 2.2* (W3C Recommendation).
https://www.w3.org/TR/WCAG22/
