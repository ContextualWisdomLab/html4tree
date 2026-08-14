# Bidirectional file-name rendering

- Date: 2026-08-14
- Scope: generated directory headings and visible file or directory names

## User problem

A file name can combine right-to-left scripts, left-to-right extensions, digits, punctuation, and spaces. Rendering that text solely in the surrounding Korean or left-to-right document direction can make the visible order ambiguous even though the stored file name remains unchanged.

## Standards decision

`html4tree` keeps the document language and surrounding interface direction unchanged. It adds `dir="auto"` only to the containers that render user-controlled directory and entry names. The browser then determines the base direction from the element content while the program continues to escape the text and URL-encode link paths through the existing functions.

This is presentation metadata, not a transformation of the file name. The implementation must not insert directional control characters into the stored name, `href`, `aria-label`, or filesystem path.

## Verification contract

- Include a directory name whose first strong character is right-to-left and which also contains digits.
- Include a file name combining Hebrew or Arabic text with a Latin extension.
- Assert `dir="auto"` on the directory heading and visible entry-name container.
- Assert that the original logical-order file name remains present in the accessible label.
- Retain the existing HTML escaping and URL-encoding regression suite.

## References

Unicode Consortium. (2025). *Unicode Standard Annex #9: Unicode bidirectional algorithm* (Revision 51). https://www.unicode.org/reports/tr9/

WHATWG. (2026). *HTML living standard: The dir attribute*. https://html.spec.whatwg.org/multipage/dom.html#the-dir-attribute

World Wide Web Consortium. (n.d.). *Structural markup and right-to-left text in HTML*. https://www.w3.org/International/questions/qa-html-dir
