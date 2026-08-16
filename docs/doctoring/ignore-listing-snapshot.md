# Ignore-listing snapshot contract

Status: **IMPLEMENTED**

## Decision

`process_ignore_file` evaluates `dirFilesNames ?: curr_dir.list()` once and
reuses that array for `.html4ignore` glob matching and default sensitive-name
filtering. Callers that already enumerated the directory must pass those names
so this function does not open the directory again.

`process_dir` now does the same on its fallback path: it lists once with
`File.listFiles()`, builds the exclusion set from those names, and renders
from that `File` array. When `listFiles()` returns null, it passes an empty
name array so `File.list()` is not consulted and the page stays empty.

## Buyer-visible contract

A directory that contains meeting notes, a leaked `.env`, and a `*.tmp` ignore
rule must generate an `index.html` that lists the notes file and omits the
secret and ignored temp file. Operators should run `java -jar html4tree.jar
<topdir>` and open the generated page before publishing. Confirm the notes
file appears as `href="./minutes.txt"` with `title="minutes.txt 파일"`.

The CLI crawl (`go` → `crawl_directories`) already passes names from one
`listFiles()` snapshot. Direct `process_dir(dir)` calls now share that same
snapshot instead of listing once for ignore and again for render.

## Threat and failure model

Two separate `File.list()` calls can observe different directory contents
(Bishop & Dilger, 1996). That TOCTOU gap can hide a newly created secret from
one filter while exposing it to the other, or the reverse. One snapshot makes
both filters agree. This is not a substitute for symlink refusal, ignore-file
size limits, or HTML escaping.

`.html4ignore` lines are Java NIO `glob:` patterns, not anchored regular
expressions (Oracle, 2021). Invalid globs are skipped.

## Verification contract

`IgnoreDirectoryListingTest` asserts:

1. omitted names → exactly one `File.list()` and `excluded.tmp` hidden
2. supplied names → zero `File.list()` calls and the same hide/keep set
3. no ignore file → one listing, `.env` hidden, `minutes.txt` kept
4. `list()` returns null → default exclusions, no crash
5. `process_dir` fallback writes HTML with `href="./minutes.txt"` and
   `title="minutes.txt 파일"`, omits `scratch.tmp` / `.env`, calls
   `File.listFiles()` once, and does not call `File.list()`
6. `process_dir` with a null `listFiles()` result writes the empty-directory
   status and does not call `File.list()`

## Rollback and recovery

Rollback restores the two Elvis `list()` evaluations, restores the
`process_dir` fallback that listed ignore names separately from render
files, removes the snapshot tests, and updates this record plus
`CHANGELOG.md`. Filtering rules stay the same; only listing cardinality
and snapshot identity change.

## Reference

Bishop, M., & Dilger, M. (1996). Checking for race conditions in file accesses.
*Computing Systems, 9*(2), 131–152.

Oracle. (2021). *Interface PathMatcher*. Java SE 11 API Specification.
https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/file/PathMatcher.html
