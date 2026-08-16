# Bidirectional isolation for generated directory names

## Decision

html4tree's generated `index.html` is opened by people whose trees contain
Arabic, Hebrew, and mixed-script names next to Korean chrome. The listing
must show the real filename, keep the Korean type label in document order,
and remain translatable.

The contract is:

1. The directory heading and each visible entry name use `dir="auto"`.
2. Those names also receive `unicode-bidi: isolate` via `h1` / `.entry-name`.
3. File/directory type text stays in a sibling `.visually-hidden` span so
   browser translation can reach it. Do not put that text inside the
   isolated name, and do not restore entry `aria-label`s.
4. `title` attributes have no `dir`, so the filename is wrapped with
   First Strong Isolate (U+2068) and Pop Directional Isolate (U+2069)
   before the Korean type label.
5. Hover and keyboard-focus underlines target `.entry-name`. After size
   and last-modified metadata landed, `span:last-child` may be
   `.entry-meta` or the bidi-control warning. Do not underline
   `span:last-child`.

Filename bidirectional format controls are neutralized before isolation.
See `docs/doctoring/bidi-control-neutralization.md`.

## Why `dir="auto"` alone is not enough

WHATWG HTML maps `dir="auto"` to a directional isolate whose paragraph
direction is taken from the element's own first strong character. That
protects the element's text. It does not protect:

- adjacent siblings in the same flex row (the Korean type label);
- attribute values such as `title`, which are plain text.

Unicode Standard Annex #9 defines explicit isolates for those leftover
plain-text contexts. html4tree therefore emits FSI/PDI only in `title`.

## Verification contract

`BidiIsolationTest` writes real Arabic (`تقرير.pdf`), Hebrew
(`חשבונית.txt`), and mixed (`회의-محضر-2026.txt`) files, then checks the
generated page for:

- isolated `dir="auto"` name spans;
- sibling Korean `.visually-hidden` type labels;
- FSI/PDI in `title`;
- percent-encoded UTF-8 `href`s that still resolve to those files.

`GeneratedIndexReadabilityTest` checks that the stylesheet isolates
`.entry-name` and underlines that class, not `span:last-child`.
`CspHashTest` still hashes the exact emitted `<style>` bytes.

## Next action for operators

After upgrading, regenerate the tree (`java -jar html4tree.jar <topdir>`)
and open a directory that contains at least one Arabic or Hebrew name.
Confirm the name reads in its natural direction and the Korean "파일" /
"디렉토리" label stays on the trailing side of the row.

## References

Goregaokar, M., & Leroy, R. (2025, August 13). *Unicode bidirectional
algorithm* (Unicode Standard Annex #9, Unicode 17.0.0, revision 51).
The Unicode Consortium. https://www.unicode.org/reports/tr9/tr9-51.html

World Wide Web Consortium. (n.d.). *The `dir` attribute*. In *HTML
Living Standard*. WHATWG. https://html.spec.whatwg.org/multipage/dom.html#the-dir-attribute

Ishida, R. (2021, November 2). *Structural markup and right-to-left text
in HTML*. World Wide Web Consortium. https://www.w3.org/International/questions/qa-html-dir

World Wide Web Consortium. (2024, December 12). *Web Content
Accessibility Guidelines (WCAG) 2.2* (W3C Recommendation).
https://www.w3.org/TR/WCAG22/
