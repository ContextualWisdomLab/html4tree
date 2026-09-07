# Architecture

html4tree is a single-process Kotlin CLI. It walks one directory tree and
writes a static `index.html` into each visited directory. There is no
database, no HTTP server, and no JavaScript runtime in the generated page.

```
TOPDIR
  -> go() validates the root (exists, not a symlink, not filesystem root)
  -> crawl_directories() BFS queue (util.kt)
       -> one listFiles() snapshot (null becomes empty name + file arrays)
       -> process_ignore_file() builds the exact-name exclusion set
       -> process_dir() renders one listing from the same snapshot
       -> crawl root omits href="./.."; nested directories keep it
       -> write_index_file() temp file + move (never follows a symlinked index.html)
```

## Product boundary

| In scope | Out of scope |
| --- | --- |
| Static directory listings a person can browse | Storybook / Figma component libraries (no JS app) |
| Safe HTML/CSS for mixed-script names | Psychometrics, GPU kernels, or LLM orchestration |
| Local CLI used alone or wrapped by another repo | A required central service |

The generated row (icon + isolated name + translatable type label +
size + UTC mtime) is the repeating web object. Its colors live in
`--listing-*` tokens inside `CSS_CONTENT`. Do not add a second
stylesheet copy; `CspHashTest` hashes the exact emitted bytes.

## Modular use

- **Alone:** `java -jar html4tree.jar <topdir>`.
- **As a module:** another repo can shell out to the fat jar. Keep the
  CLI contract (`TOPDIR`, `--max-level`) stable. Do not require naruon
  or the org `.github` workflows to produce a listing.

## Security and accessibility invariants

See `CLAUDE.md`. Bidirectional isolation is recorded in
`docs/doctoring/bidi-isolation.md`. Control neutralization is recorded
in `docs/doctoring/bidi-control-neutralization.md`. Size and mtime are
recorded in `docs/doctoring/listing-entry-metadata.md`. The shared
ignore/render snapshot is recorded in
`docs/doctoring/ignore-listing-snapshot.md`.

## Data model

html4tree has no persistent schema. The only durable objects are
generated `index.html` files and optional `.html4ignore` inputs. There
are no database identifiers to rename.

## Goal

Ship directory listings that a buyer can open on a phone or desktop and
immediately find the next file, including when names are Arabic, Hebrew,
or mixed with Korean chrome, when two files share a similar name, and
when a filename tries to hide its extension with bidirectional controls,
and without a parent link that leaves the published tree.
