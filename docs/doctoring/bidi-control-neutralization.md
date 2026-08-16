# Bidirectional control neutralization for displayed names

## Decision

html4tree isolates mixed-script filenames so Korean chrome stays readable
(`docs/doctoring/bidi-isolation.md`). Isolation is not enough when the
filename itself contains Unicode bidirectional format controls. Those
controls can hide an extension (Trojan Source): `invoice.txt` + U+202E +
`exe` can look like a text file.

The contract is:

1. Detect bidirectional format controls in the observed `File.name`
   (U+061C, U+200E, U+200F, U+202A–U+202E, U+2066–U+2069).
2. Replace each control with U+FFFD in the *display* name and in the
   plain-text `title` payload. Then wrap that cleaned name with FSI/PDI.
3. Keep the real filesystem name in `href` (percent-encoded) so the
   generated link still opens the file.
4. When any control was replaced, emit a sibling
   `.visually-hidden` warning (`이름에 방향 제어 문자가 있습니다`) so
   browser translation can reach it.
5. Do **not** strip U+2066–U+2069 inside `escapeHtml()`. That would
   delete the FSI/PDI marks the listing adds after neutralization.

This is a display-time neutralization. It does not rename files on disk.

## Why not strip inside `escapeHtml()`

PR #454 proposed removing U+202A–U+202E and U+2066–U+2069 from every
string that passes through `escapeHtml()`. After the BiDi isolation
change, `title` values intentionally contain U+2068 and U+2069. A global
strip would undo isolation and re-open the Korean-label reorder bug.

UTS #39 requires that displayed identifiers not contain stateful
bidirectional format characters. UTR #36 §2.5 describes the same
spoofing class. Boucher and Anderson (2022) document the extension-hiding
pattern this listing must not present as a safe name.

## Verification contract

`TrojanSourceSecurityTest` writes a real file named `invoice.txt` +
U+202E + `exe` and checks the generated page for:

- U+FFFD in the isolated `.entry-name`;
- FSI/PDI around the cleaned name in `title`;
- the Korean warning span;
- a percent-encoded `href` that still matches the real filesystem name;
- no raw U+202E in the visible name.

`BidiIsolationTest` still opens real Arabic and Hebrew names that contain
no format controls. `CspHashTest` is unchanged.

## Next action for operators

Regenerate the tree (`java -jar html4tree.jar <topdir>`). If a row shows
U+FFFD replacement characters and the hidden warning, treat that file as
untrusted: do not infer its type from the visible name; open it only
after inspecting the real name on disk (`ls -b` or equivalent).

## References

Boucher, N., & Anderson, R. (2022). Trojan Source: Invisible
vulnerabilities. In *31st USENIX Security Symposium (USENIX Security 22)*
(pp. 3165–3182). USENIX Association.
https://www.usenix.org/conference/usenixsecurity22/presentation/boucher

Davis, M., & Suignard, M. (2014, September 19). *Unicode security
considerations* (Unicode Technical Report #36, revision 15). The Unicode
Consortium. https://www.unicode.org/reports/tr36/

Davis, M., & Suignard, M. (2025, September 4). *Unicode security
mechanisms* (Unicode Technical Standard #39, Version 17.0.0, revision
32). The Unicode Consortium. https://www.unicode.org/reports/tr39/

Goregaokar, M., & Leroy, R. (2025, August 13). *Unicode bidirectional
algorithm* (Unicode Standard Annex #9, Unicode 17.0.0, revision 51).
The Unicode Consortium. https://www.unicode.org/reports/tr9/tr9-51.html
