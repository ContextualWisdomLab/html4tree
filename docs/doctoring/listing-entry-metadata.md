# Size and last-modified metadata on generated rows

## Decision

A purchaser opening a generated listing must be able to choose among
similarly named files. Apache HTTP Server `mod_autoindex` FancyIndexing
exposes Name, Last Modified, and Size. html4tree already reads
`BasicFileAttributes` once per entry to decide directory versus symlink.
That same snapshot now supplies:

1. **Size** for regular files, formatted with IEC 80000-13 binary
   prefixes (`B`, `KiB`, `MiB`, `GiB`) and one decimal place above
   1023 bytes. Directories use an em dash (U+2014) because directory
   `size()` is an implementation-defined inode value, not payload size.
2. **Last modified** as a `<time datetime>` whose `datetime` is the
   ISO 8601 instant (`Instant.toString()`) and whose visible text is
   `yyyy-MM-dd HH:mm UTC` formatted with `Locale.ROOT`. Static pages
   have no viewer timezone, so the visible `UTC` label is required.
3. Both values live in a sibling `.entry-meta` group *outside* the
   isolated `.entry-name`, with `direction: ltr` and
   `unicode-bidi: isolate`, so Arabic or Hebrew names cannot reorder
   the numbers.
4. Colors use `--listing-meta` / `--listing-dark-meta` (the same
   contrast-tested pair as empty-state text).
5. If `readAttributes` fails (vanished or unreadable entry), the row
   still renders the name and omits metadata rather than inventing
   zeros.

Parent `..` and the empty-directory status row do not show size or
mtime.

## Why this belongs in the generated row

The product promise is that a person can open `index.html` and pick the
next file. Name isolation alone does not distinguish `report-final.pdf`
at 12 KiB from a 2.1 MiB replacement written later the same day. Putting
size and mtime inside the same `<a>` keeps them in the accessible name
without a second control.

## Verification contract

`ListingMetadataTest` writes a real `minutes.txt` whose UTF-8 payload is
`hello world` (11 bytes), then asserts the generated page contains
`11 B` and a `<time>` whose `datetime` equals that file's
`lastModifiedTime`. A real subdirectory asserts the em-dash size. A
ghost path that is listed but missing on disk asserts the name is
present and `.entry-meta` is absent.

`GeneratedIndexReadabilityTest` checks the new tokens and that authored
meta colors still meet the 4.5:1 text-contrast threshold. `CspHashTest`
rehashes the exact emitted `<style>` bytes.

## Next action for operators

Regenerate the tree (`java -jar html4tree.jar <topdir>`). Confirm one
file row shows a byte size that matches `wc -c` / `stat` and a UTC
timestamp that matches the filesystem mtime. If you publish the tree,
tell readers that times are UTC.

## References

Apache Software Foundation. (n.d.). *Apache module mod_autoindex*
(Version 2.4).
https://httpd.apache.org/docs/2.4/mod/mod_autoindex.html

International Electrotechnical Commission. (2008). *Quantities and units
— Part 13: Information science and technology* (IEC 80000-13:2008).

International Organization for Standardization. (2019). *Date and time —
Representations for information interchange — Part 1: Basic rules*
(ISO 8601-1:2019).

World Wide Web Consortium. (n.d.). *The `time` element*. In *HTML Living
Standard*. WHATWG.
https://html.spec.whatwg.org/multipage/text-level-semantics.html#the-time-element

World Wide Web Consortium. (2024, December 12). *Web Content
Accessibility Guidelines (WCAG) 2.2* (W3C Recommendation).
https://www.w3.org/TR/WCAG22/
