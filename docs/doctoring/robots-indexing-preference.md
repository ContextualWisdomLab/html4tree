# Generated-page robots indexing preference

Status: **IMPLEMENTED-ON-PROTECTED-MAIN**

## Decision

Every generated directory `index.html` emits this directive in its document head:

```html
<meta name="robots" content="noindex, nofollow">
```

The directive asks crawlers that support robots meta rules not to index the page or follow links from it. The same directive is emitted for empty and non-empty generated directories so the behavior does not depend on directory contents.

## Authority and non-goals

This is a discoverability preference, not authentication, authorization, access control, confidentiality, deletion from an existing search index, or a crawler firewall. Supporting crawlers must fetch and read the page before they can observe `noindex`. A `robots.txt` rule that prevents fetching can therefore stop the crawler from seeing this directive. Other crawlers may interpret or ignore the rule differently.

Operators must remove confidential material from the generated tree or protect it at the serving boundary. This project does not claim that the meta tag prevents discovery, crawling, direct access, link sharing, archival, or indexing by every service.

## Verification contract

`MainTest` generates both an empty directory page and a page containing entries, then applies one shared assertion to the exact emitted directive. The test-only predecessor commit failed both generated-page cases before production emitted the directive. PR #425 integrated the contract on protected `master` at commit `b939ca735bb565fe8b5158823710ea96483d227a`; later source changes remain subject to exact-head CI, Security Scan, SAST, and repository review policy.

## Rollback and recovery

Rollback removes the single robots meta element and the two generated-page assertions together, then updates this decision record and `CHANGELOG.md`. Removing the directive restores the default crawler behavior; it does not grant or revoke access to the generated files.

## Reference

Google Search Central. (2025, December 10). *Block Search indexing with noindex*. Google for Developers. https://developers.google.com/search/docs/crawling-indexing/block-indexing
