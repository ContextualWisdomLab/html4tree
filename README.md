# html4tree

[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/ContextualWisdomLab/html4tree)

**Generate browsable static `index.html` pages from a directory tree.**

`html4tree` is a small Kotlin command-line tool for turning a filesystem tree into self-contained directory indexes, similar in purpose to Apache `mod_autoindex` but generated ahead of time. Point it at a directory, choose how deep it may recurse, and it writes navigation pages that can be served by an ordinary static file host.

It is useful when you want lightweight directory browsing without running a dynamic directory-listing service. It does **not** start a web server, manage authentication, or decide which files are safe to publish; the operator remains responsible for the content under the selected root.

## Quick start

The checked-in wrapper uses Gradle 5.1.1. Use a JDK 8–11 runtime with this repository's current build toolchain; newer JDKs require a separate, reviewed Gradle/Kotlin toolchain upgrade rather than an undocumented local workaround.

Build the executable JAR with the checked-in Gradle wrapper:

```bash
./gradlew build
```

Then inspect the CLI contract:

```bash
java -jar ./build/libs/html4tree.jar -h
```

Generate indexes recursively beneath a directory:

```bash
java -jar ./build/libs/html4tree.jar /path/to/static-tree
```

Limit generation to the selected top directory only:

```bash
java -jar ./build/libs/html4tree.jar /path/to/static-tree --max-level 0
```

`TOPDIR` is the directory to crawl. `--max-level` bounds how many directory levels receive generated `index.html` files.

## Exclude content from an index

Place a `.html4ignore` file in a directory to exclude matching entries from that directory's generated index. Each non-empty line is interpreted as a Java filesystem glob matched against the entry name.

For example, to exclude text files from the generated listing:

```text
*.txt
```

When a matching entry is a directory, the current crawler also does not enqueue that directory for recursion, so `html4tree` does not generate indexes beneath it during that crawl. The directory and its files remain on disk, and a static server may still serve them when addressed directly.

The parser deliberately bounds ignore input: patterns longer than 100 characters and lines after the first 1,000 are ignored. `.html4ignore` therefore affects generated navigation and crawler traversal; it is **not** an authorization boundary. Do not place sensitive material under a published static root merely because it is excluded from a listing or recursive generation.

## Operating model

`html4tree` reads the selected directory tree and writes generated `index.html` files into that tree. The resulting pages are static output and can be served by any suitable static host. The tool does not own HTTP serving, access control, TLS, deployment, or content-retention policy.

If you need to remove generated indexes from a tree, review the target carefully before using a filesystem command such as:

```bash
find /path/to/static-tree -name index.html -delete
```

That command is an operator action outside `html4tree`; make sure pre-existing `index.html` files are not being mistaken for generated output.

## Development and verification

The current Gradle build uses Kotlin and Clikt for the CLI and JUnit/Kotlin test tooling. Run the repository verification path with:

```bash
./gradlew check
```

The build config wires JaCoCo coverage verification into `check` with a 100% minimum coverage threshold for the measured code. Passing source on one machine is not a substitute for checking the exact revision and workflow evidence you intend to ship.

For vulnerability reporting, follow [`SECURITY.md`](SECURITY.md). Do not disclose suspected vulnerabilities in a public issue.

## Project documentation

- [`CHANGELOG.md`](CHANGELOG.md) — integrated change history.
- [`SECURITY.md`](SECURITY.md) — coordinated vulnerability-reporting boundary.
- [`AGENTS.md`](AGENTS.md) and [`CLAUDE.md`](CLAUDE.md) — maintainer/automation guidance, not end-user product behavior.
- [`docs/doctoring/`](docs/doctoring/) — deeper maintenance evidence where applicable.

## Contributing

Keep changes small and deterministic. User-visible behavior changes should include tests for generated navigation and exclusion semantics, and the full `./gradlew check` contract should remain green. Avoid moving maintainer automation procedure into the customer-facing README.

## License

`html4tree` is distributed under the **MIT License**. See [`LICENSE`](LICENSE).

The existing license retains the original copyright notice for Yamir Encarnacion. ContextualWisdomLab maintenance does not replace that attribution or relicense independently licensed dependencies; third-party Kotlin/Gradle/test libraries remain under their own terms.
