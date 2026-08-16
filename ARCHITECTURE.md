# Architecture

html4tree is a standalone Kotlin CLI. It walks one directory tree and writes a
static `index.html` in each visited directory. There is no database, no HTTP
server, and no in-process consumer in other ContextualWisdomLab products. Keep
the jar usable alone; do not add a service mesh, Storybook, or Figma surface.

## Runtime flow

1. `Html4tree` parses `TOPDIR`, `--max-level`, `--force-overwrite`,
   `--cleanup`, and `--dry-run`. `--dry-run` without `--cleanup` is rejected
   before any walk starts.
2. `go` refuses a blank path, `..`, filesystem root, a missing directory, and
   a symlink top directory (`LinkOption.NOFOLLOW_LINKS`).
3. `crawl_directories` walks breadth-first through the FIFO in `util.kt`.
   Symlink children are not enqueued.
4. Generation calls `process_dir` → `write_index_file`. Cleanup calls
   `cleanup_owned_index` on the same walk.

## Ownership write path

```
classify index.html
        │
        ├─ UNSAFE / UNOWNED (no --force-overwrite) → preserve
        ├─ ABSENT → reclassify → create-only move (no ATOMIC_MOVE, no REPLACE)
        └─ OWNED or forced UNOWNED
                → same-directory backup
                → reclassify
                → ATOMIC_MOVE, then REPLACE_EXISTING only if still replaceable
                → on failure, restore backup or report backup-retained:
```

Cleanup classifies, then classifies again immediately before `Files.delete`.
A target that stopped being owned is preserved.

## Invariants

- Never follow symlinks for root checks, child traversal, prefix reads, or
  publication.
- Bound `.html4ignore` to a regular non-symlink file, ≤ 1 MB, ≤ 1000 lines,
  ≤ 100 characters per pattern.
- Escape names for HTML text and URL-encode `href`s. Keep the CSP meta tag.
- Prefix inspection reads at most `OWNERSHIP_PREFIX_LIMIT` bytes with a
  JDK 8-safe loop. Do not call `InputStream.readNBytes`.
- JaCoCo `minimum = 1.00` is the quality gate (`./gradlew check`).
