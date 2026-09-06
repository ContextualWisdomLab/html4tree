# html4tree

[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/ContextualWisdomLab/html4tree)

`html4tree` is a Kotlin command-line tool that generates static, browsable `index.html` pages for directory trees. It is useful when a filesystem needs lightweight directory navigation without operating a dynamic listing service.

## Start here

The checked-in Gradle 5.1.1 wrapper should currently be used with JDK 8–11. Build the project with:

```bash
./gradlew build
```

`build` already includes the repository verification lifecycle. When you only need verification without the rest of the build lifecycle, run `./gradlew check` instead.

For the full CLI examples, recursive-generation options, depth controls, `.html4ignore` behavior, and operator safety notes, see the [repository README](https://github.com/ContextualWisdomLab/html4tree#readme).

## Product boundary

`html4tree` reads a directory tree and writes static navigation pages. It does not run a web server, authenticate users, authorize publication, or decide whether files beneath a selected root are safe to expose. Static hosting, access control, retention, and cleanup remain operator responsibilities.

An ignored directory is omitted from generated navigation and from recursive index generation for that crawl, but remains on disk and may still be served directly. `.html4ignore` is therefore not an authorization mechanism.

## Architecture

The project is intentionally small: Kotlin implements the CLI and tree-to-HTML generation path, Clikt provides command-line parsing, and Gradle owns build/test packaging. Generated HTML is the product artifact; no long-running service or database is required.

The dated [product and technical gap baseline](product-technical-gap-baseline.md) records the current Context Map, generation flow, inherited-license boundary, release status, and active security/toolchain/product gaps without treating open branches as shipped behavior.

## Verification and maintenance

Use `./gradlew check` as the repository-level verification entry point when you do not need the complete build lifecycle. Security reporting follows the [repository security policy](https://github.com/ContextualWisdomLab/html4tree/security/policy). Repository changes should preserve the existing MIT license and upstream attribution in the [repository license](https://github.com/ContextualWisdomLab/html4tree/blob/master/LICENSE).

## Releases and source truth

The ContextualWisdomLab fork currently has no GitHub Release. Treat source state and current CI as revision evidence rather than as an immutable release artifact. Documentation on this site does not itself constitute a release, hosted-service SLA, Pages publication, or security certification.

## Learn more

- [README](https://github.com/ContextualWisdomLab/html4tree#readme) — task-first usage and operating model
- [Product and technical gap baseline](product-technical-gap-baseline.md) — current architecture, release, security, and maintenance gaps
- [Security policy](https://github.com/ContextualWisdomLab/html4tree/security/policy) — vulnerability reporting and security boundary
- [License](https://github.com/ContextualWisdomLab/html4tree/blob/master/LICENSE) — inherited MIT grant and attribution
- [Ask DeepWiki](https://deepwiki.com/ContextualWisdomLab/html4tree) — repository-aware navigation and questions
