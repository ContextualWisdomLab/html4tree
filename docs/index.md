# html4tree

[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/ContextualWisdomLab/html4tree)

`html4tree` is a Kotlin command-line tool that generates static, browsable `index.html` pages for directory trees. It is useful when a filesystem needs lightweight directory navigation without operating a dynamic listing service.

## Start here

Build and verify the project with the repository's Gradle wrapper:

```bash
./gradlew check
./gradlew shadowJar
```

For the full CLI examples, recursive-generation options, depth controls, `.html4ignore` behavior, and operator safety notes, see the [repository README](../README.md).

## Product boundary

`html4tree` reads a directory tree and writes static navigation pages. It does not run a web server, authenticate users, authorize publication, or decide whether files beneath a selected root are safe to expose. Static hosting, access control, retention, and cleanup remain operator responsibilities.

## Architecture

The project is intentionally small: Kotlin implements the CLI and tree-to-HTML generation path, Clikt provides command-line parsing, and Gradle owns build/test packaging. Generated HTML is the product artifact; no long-running service or database is required.

## Verification and maintenance

Use `./gradlew check` as the repository-level verification entry point. Security reporting follows [`SECURITY.md`](../SECURITY.md). Repository changes should preserve the existing MIT license and upstream attribution in [`LICENSE`](../LICENSE).

## Releases and source truth

Treat the protected default branch and GitHub release/tag history as the source of truth for the version you deploy. Documentation on this site describes repository capabilities; it does not itself constitute a release, hosted-service SLA, or security certification.

## Learn more

- [README](../README.md) — task-first usage and operating model
- [Security policy](../SECURITY.md) — vulnerability reporting and security boundary
- [Ask DeepWiki](https://deepwiki.com/ContextualWisdomLab/html4tree) — repository-aware navigation and questions
