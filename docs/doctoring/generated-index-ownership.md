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
- Ownership inspection reads only the configured prefix through a no-follow input stream. It does not read an arbitrarily large existing page into memory merely to find the marker. The bounded read is a JDK 8-compatible loop, not `InputStream.readNBytes`.
- `ATOMIC_MOVE` is attempted first. `REPLACE_EXISTING` is used only after the current occupant is reclassified as replaceable (owned, or unmarked with `--force-overwrite`). An unowned occupant that appears during that window aborts the write.
- `--force-overwrite` is an opt-in destructive switch for unmarked regular files. It never authorizes replacing a symbolic link or a directory.
- If publication fails, html4tree attempts to restore the backup. If restoration also fails, the backup is retained and its exact path is reported as `backup-retained:` so an operator can recover it explicitly. Recover the retained backup before another destructive operation.

## Cleanup and migration

- `--cleanup` deletes only files whose prefix contains a valid `html4tree/1` marker.
- `--dry-run` reports the same owned set without deleting and is rejected unless `--cleanup` is also supplied.
- Pre-marker outputs are intentionally unowned. Ownership is not inferred from HTML structure, titles, or CSS. Operators who must adopt a known unmarked page use `--force-overwrite` after an independent backup.

## Buyer-visible contract

A mixed tree can contain a hand-written home page next to generated listings. After `java -jar html4tree.jar <topdir>`, open the customer page and confirm the original markup is unchanged. Open one generated page and confirm the generator marker is present near the start. To remove only generated pages:

```bash
java -jar html4tree.jar --cleanup --dry-run <topdir>
java -jar html4tree.jar --cleanup <topdir>
```

## Rollback

Removing the marker emission and the classify-before-write gate restores the previous replace-by-filename behavior and reintroduces customer data-loss risk. Rollback must also restore the README warning if the destructive `find` command is ever reintroduced.

## Verification

`GeneratedIndexOwnershipTest` and `GeneratedIndexOwnershipContractTest` cover user-authored preservation, owned replacement, late and malformed markers, symlink refusal, bounded-prefix EOF handling, CLI misuse rejection, atomic-conflict refusal, backup failure, publication restore, retained-backup reporting, mixed nested sites, and cleanup/dry-run selection of the same owned set.

## References

MITRE. (n.d.). *CWE-73: External control of file name or path*. https://cwe.mitre.org/data/definitions/73.html

Oracle. (2021). *Enum class StandardCopyOption*. Java SE 11 API Specification. https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/file/StandardCopyOption.html

Oracle. (n.d.). *Moving, copying, and deleting files*. Dev.java. https://dev.java/learn/java-io/file-system/move-copy-delete/
