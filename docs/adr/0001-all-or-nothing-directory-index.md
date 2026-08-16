# ADR 0001: Treat each generated directory index as an atomic security artifact

- Status: Accepted
- Date: 2026-08-14

## Context

`html4tree` publishes file and directory names through a generated `index.html`. If metadata validation fails for only one candidate entry, continuing with the readable entries creates a partial listing. The difference between present and absent entries can disclose which paths were readable, and an older generated index can continue exposing names that the current run could no longer validate.

A directory index is therefore not a collection of independent best-effort rows. It is one externally visible security artifact whose complete candidate set must be validated before publication.

## Decision

For every directory processed by `process_dir`:

1. Determine the candidate entries that would be considered for publication.
2. Read and validate the required metadata for every candidate.
3. Generate and atomically replace `index.html` only when all candidates were validated.
4. If any candidate metadata cannot be read, abort publication for the complete directory and delete a stale generated `index.html` if one exists.
5. Do not generate a replacement page containing the failed filename, the number of failures, or exception details.

This decision applies to metadata-read failures. Existing ignore rules, symlink policy, output escaping, and atomic write behavior remain separate controls.

## Consequences

- A directory with one unverifiable candidate becomes temporarily unavailable as an HTML listing instead of exposing a partial view.
- A later successful run recreates the full index.
- Operators must diagnose failures from trusted execution logs rather than from public HTML output.
- Regression tests must include a readable entry, an unverifiable entry, and a stale generated index, and must prove that no partial or stale index survives.

## Rejected alternatives

### Skip only the failed entry

Rejected because the output reveals the readable subset and creates a visible side channel tied to metadata access.

### Keep the previous generated index

Rejected because a stale index may expose names that the current execution can no longer validate.

### Publish an error page with path details

Rejected because public output is not an appropriate diagnostic channel for filesystem paths or exception data.
