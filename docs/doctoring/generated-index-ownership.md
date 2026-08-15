# Generated index ownership

Status: **IMPLEMENTED**

## Decision

html4tree treats `index.html` as a generated artifact only when the file carries a versioned ownership marker near the start of the document:

```html
<meta name="generator" content="html4tree/1">
```

Filename identity is not ownership. Atomic replacement is not ownership. An existing unmarked, malformed, unsupported-version, late-marker, symbolic-link, directory, or unreadable `index.html` is preserved by default.

## Generation

- Absent targets may be created.
- Valid owned regular files may be replaced. The writer copies the owned file to a hidden same-directory backup, publishes the new document, revalidates the occupant, and deletes the backup only after publication completes.
- `ATOMIC_MOVE` is attempted first. `REPLACE_EXISTING` is used only after the current occupant is reclassified as replaceable (owned, or unmarked with `--force-overwrite`). An unowned occupant that appears during that window aborts the write.
- `--force-overwrite` is an opt-in destructive switch for unmarked regular files. It never authorizes replacing a symbolic link or a directory.

## Cleanup and migration

- `--cleanup` deletes only files whose prefix contains a valid `html4tree/1` marker.
- `--dry-run` reports the same owned set without deleting.
- Pre-marker outputs are intentionally unowned. Ownership is not inferred from HTML structure, titles, or CSS. Operators who must adopt a known unmarked page use `--force-overwrite` after an independent backup.

## Rollback

Removing the marker emission and the classify-before-write gate restores the previous replace-by-filename behavior and reintroduces customer data-loss risk. Rollback must also restore the README warning if the destructive `find` command is ever reintroduced.

## Verification

`GeneratedIndexOwnershipTest` covers user-authored preservation, owned replacement, late and malformed markers, symlink refusal, atomic-conflict refusal, backup failure, publication restore, mixed nested sites, and cleanup/dry-run selection of the same owned set.

## References

MITRE. (n.d.). *CWE-73: External control of file name or path*. https://cwe.mitre.org/data/definitions/73.html

Oracle. (n.d.). *Enum class StandardCopyOption*. Java SE 21. https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/file/StandardCopyOption.html

Oracle. (n.d.). *Moving, copying, and deleting files*. Dev.java. https://dev.java/learn/java-io/file-system/move-copy-delete/
