# Ignore-listing snapshot contract

Status: **IMPLEMENTED-ON-THIS-BRANCH**

## Decision

`process_ignore_file` evaluates `dirFilesNames ?: curr_dir.list()` once and
reuses that array for `.html4ignore` glob matching and default sensitive-name
filtering. Callers that already enumerated the directory must pass those names
so this function does not open the directory again.

## Buyer-visible contract

A directory that contains meeting notes, a leaked `.env`, and a `*.tmp` ignore
rule must generate an `index.html` that lists the notes file and omits the
secret and ignored temp file. Operators should run `java -jar html4tree.jar
<topdir>` and open the generated page before publishing.

The CLI crawl (`go` → `crawl_directories`) already passes names from one
`listFiles()` snapshot. The reused `list()` path is the fallback used by
`process_dir` when no exclusion set is supplied, and by direct calls.

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
5. `process_dir` fallback writes HTML that keeps `minutes.txt` and omits
   `scratch.tmp` / `.env`

## Rollback and recovery

Rollback restores the two Elvis `list()` evaluations, removes the snapshot
tests, and updates this record plus `CHANGELOG.md`. Filtering rules stay the
same; only listing cardinality and snapshot identity change.

## Reference

Bishop, M., & Dilger, M. (1996). Checking for race conditions in file accesses.
*Computing Systems, 9*(2), 131–152.

Oracle. (2021). *Interface PathMatcher*. Java SE 11 API Specification.
https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/file/PathMatcher.html
