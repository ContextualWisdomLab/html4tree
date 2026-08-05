# Inline style CSP byte-identity contract

## Decision

html4tree emits one inline `<style>` block in every generated `index.html` file.
The stylesheet is normalized once, encoded as UTF-8, and hashed with SHA-256.
The exact same normalized string is then inserted between the `<style>` tags.
Template indentation, leading newlines, and trailing newlines are not added to the
hashed source.

This is a byte-identity requirement rather than a merely visual CSS-equivalence
requirement. Whitespace and capitalization changes can preserve CSS rendering
semantics while changing the Content Security Policy hash.

## Threat and failure model

The generated document uses a restrictive policy with `default-src 'none'` and a
single `style-src` hash-source. If the declared digest does not match the exact
inline style bytes, a conforming user agent blocks the stylesheet. The result is
an availability and integrity failure: users receive an unstyled directory index
and may lose focus, contrast, or reduced-motion behavior that the stylesheet was
intended to provide.

The fix does not add `unsafe-inline`, a nonce, a network stylesheet, or a broader
source expression. It also does not treat CSP as a substitute for HTML escaping,
path validation, or filesystem access controls.

## Standards interpretation

Content Security Policy Level 3 defines hash matching by UTF-8 encoding the
inline source, applying the selected digest algorithm, Base64-encoding the digest,
and comparing it with the hash-source value. Inline style is blocked when the
source list does not authorize the exact block. Therefore the implementation must
hash the style element's text content, excluding the `<style>` start and end tags,
without relying on CSS-equivalent normalization by the browser.

The cited CSP Level 3 document is a W3C Working Draft and may change. This project
uses its current matching algorithm as an engineering contract and makes no claim
of formal W3C conformance.

## Verification contract

`CspHashTest.emittedStyleBytesMatchTheDeclaredCspHash` performs a product-level
round trip:

1. create a real temporary directory;
2. generate its `index.html` through `process_dir`;
3. extract the emitted style text and declared SHA-256 source expression;
4. assert that the emitted style has no template padding;
5. hash the exact UTF-8 style text independently; and
6. require byte-for-byte digest equality.

The repository build additionally enforces the existing JaCoCo coverage threshold,
CI tests, SAST, and security checks on the exact pull-request head.

## Reference

West, M., & Sartori, A. (Eds.). (2026, July 29). *Content Security Policy Level 3*
(W3C Working Draft). World Wide Web Consortium.
https://www.w3.org/TR/2026/WD-CSP3-20260729/
