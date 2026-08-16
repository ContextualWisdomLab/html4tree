# Generated index ownership

Status: **IMPLEMENTED ON ACTIVE PR**

## Decision

html4tree treats `index.html` as a generated artifact only when the file carries a versioned ownership marker near the start of the document:

```html
<meta name="generator" content="html4tree/1">
```

Filename identity is not ownership. Atomic replacement is not ownership. An existing unmarked, malformed, unsupported-version, late-marker, symbolic-link, directory, or unreadable `index.html` is preserved by default.

## Generation

- Absent targets may be created. Publication reclassifies immediately before the exclusive publish. The first attempt is `Files.createLink` (POSIX `link(2)`), which fails with `EEXIST` and does not replace an occupant. If the filesystem cannot hard-link, html4tree falls back to `Files.move` with no `ATOMIC_MOVE` and no `REPLACE_EXISTING`.
- Do not use `ATOMIC_MOVE` or `REPLACE_EXISTING` for first-time creates. POSIX `rename` and Java `ATOMIC_MOVE` may replace an existing target; that is a replace primitive, not a create-exclusive primitive. The hard-link fallback still has an intra-call `rename` window after the provider `lstat`; treat that as residual risk on filesystems without `link(2)`.
- Valid owned regular files may be replaced. The writer copies the owned file to a hidden same-directory backup, publishes the new document, revalidates the occupant, and deletes the backup only after publication completes.
- Ownership inspection reads only the configured prefix through a no-follow input stream. The prefix reader copies at most that many bytes and does not call `InputStream.readNBytes`, which is absent on JDK 8.
- For owned or `--force-overwrite` replacement, `ATOMIC_MOVE` is attempted first. `REPLACE_EXISTING` is used only after the current occupant is reclassified as replaceable. An unowned occupant that appears during that window aborts the write.
- `--force-overwrite` is an opt-in destructive switch for unmarked regular files. It never authorizes replacing a symbolic link or a directory.
- If publication fails, html4tree reclassifies the occupant before any restore. `ABSENT` restores with the exclusive publisher. `OWNED` may replace. `UNOWNED` and `UNSAFE` occupants are left in place and the backup is retained as `backup-retained:`. If restoration itself fails, the backup is retained the same way. Recover the retained backup before another destructive operation.

## Cleanup and migration

- `--cleanup` deletes only files whose prefix contains a valid `html4tree/1` marker.
- Cleanup reclassifies the same path immediately before delete. A target that stopped being owned is preserved.
- `--dry-run` reports the same owned set without deleting and is rejected unless `--cleanup` is also supplied.
- Pre-marker outputs are intentionally unowned. Ownership is not inferred from HTML structure, titles, or CSS. Operators who must adopt a known unmarked page use `--force-overwrite` after an independent backup.

## Rollback

Removing the marker emission and the classify-before-write gate restores the previous replace-by-filename behavior and reintroduces customer data-loss risk. Rollback must also restore the README warning if the destructive `find` command is ever reintroduced.

## Verification

`GeneratedIndexOwnershipTest` and `GeneratedIndexOwnershipContractTest` cover user-authored preservation, owned replacement, late and malformed markers, symlink refusal, bounded-prefix EOF handling, CLI misuse rejection, atomic-conflict refusal, backup failure, publication restore, retained-backup reporting, mixed nested sites, cleanup/dry-run selection of the same owned set, exclusive-create refusal when a customer page appears, default-publisher refusal of a real occupant, hard-link fallback, late-occupant reclassification, restore reclassification (owned / absent / late customer), and cleanup revalidation before delete including symlink and directory occupants.

## References

IEEE and The Open Group. (2018). *link — link one file to another file* (IEEE Std 1003.1-2017). https://pubs.opengroup.org/onlinepubs/9699919799/functions/link.html

IEEE and The Open Group. (2018). *rename — rename a file* (IEEE Std 1003.1-2017). https://pubs.opengroup.org/onlinepubs/9699919799/functions/rename.html

MITRE. (n.d.). *CWE-73: External control of file name or path*. https://cwe.mitre.org/data/definitions/73.html

Oracle. (n.d.). *Enum class StandardCopyOption*. Java SE 11. https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/file/StandardCopyOption.html

Oracle. (n.d.). *Files.createLink*. Java SE 11. https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/file/Files.html#createLink(java.nio.file.Path,java.nio.file.Path)

Oracle. (n.d.). *Files.move*. Java SE 11. https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/file/Files.html#move(java.nio.file.Path,java.nio.file.Path,java.nio.file.CopyOption...)

Oracle. (n.d.). *Moving, copying, and deleting files*. Dev.java. https://dev.java/learn/java-io/file-system/move-copy-delete/
