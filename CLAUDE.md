# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

html4tree is a small Kotlin CLI tool that crawls a directory tree and generates a static `index.html` listing in each directory (like Apache mod_autoindex). Files can be excluded per directory via a `.html4ignore` file containing one regex per line.

## Commands

```bash
./gradlew                                          # default task is build: compile, test, coverage check, fat jar
./gradlew test                                     # run all tests (JUnit 4)
./gradlew test --tests "html4tree.MainTest"        # run a single test class
./gradlew test --tests "html4tree.MainTest.testEscapeHtml"  # run a single test method
./gradlew check                                    # test + JaCoCo coverage verification
./gradlew jacocoTestReport                         # coverage report -> build/jacocoHtml, XML in build/reports

java -jar build/libs/html4tree.jar <topdir>              # run (fat jar, deps bundled)
java -jar build/libs/html4tree.jar <topdir> --max-level 0  # index only the top directory
```

Toolchain is old: Gradle 5.1.1 wrapper, Kotlin 1.3.72, legacy `compile`/`testCompile` configurations. Requires an older JDK (8–11); the fat jar bundles everything on `configurations.compile`, so new dependencies must be declared with `compile` (not `implementation`) or they won't be in the jar. There is no CI; `./gradlew check` is the quality gate.

**JaCoCo enforces 100% coverage** (`jacocoTestCoverageVerification`, `minimum = 1.00`, wired into `check`). Any new code or branch without a covering test fails the build.

## Architecture

Single package `html4tree`, two source files plus mirrored tests:

- `src/main/kotlin/html4tree/main.kt` — everything of substance:
  - `Html4tree : CliktCommand` (Clikt 2.7.1) parses `TOPDIR` and `--max-level`; `main()` is the entry point (`Main-Class: html4tree.MainKt`).
  - `go(topDir, maxLevel)` validates the top directory (must exist, must not be a symlink, must not be filesystem root) and iteratively walks the tree breadth-first using the queue from `util.kt`, calling `process_dir` per directory up to `maxLevel` (`-1` = unlimited).
  - `process_dir(dir)` builds the HTML listing (sorted entries, folder/file icons, aria-labels, CSP meta tag, empty-state message) and writes it via `write_index_file`, which writes to a temp file then `Files.move(..., REPLACE_EXISTING)` so a symlinked `index.html` is replaced, never followed.
  - `process_ignore_file(dir)` returns the exclusion set: `.html4ignore` regexes (anchored `^...$`) plus `index.html` plus a hardcoded sensitive-file list (`.git`, `.env`, `.ssh`, etc.).
  - `escapeHtml()` / `urlEncodePath()` string extensions sanitize every filename placed into HTML text or `href`s.
- `src/main/kotlin/html4tree/util.kt` — hand-rolled FIFO `LinkedList` of `LinkedListEntry(file, level)` used as the traversal queue.

## Conventions and invariants

The commit history (Sentinel/Bolt/Palette bots; learnings logged in `.jules/*.md`) has hardened this code deliberately — do not regress these invariants:

- **Never follow symlinks.** Use `Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)` for root and child checks; skip symlink children when traversing and rendering. Never use `File.canonicalFile` where symlink status matters (it resolves the link and defeats `NOFOLLOW_LINKS` checks) — use `absoluteFile.toPath().normalize()` instead.
- **`.html4ignore` parsing is bounded**: must be a regular non-symlink file, ≤ 1 MB, ≤ 1000 lines/patterns, ≤ 100 chars per pattern; invalid regexes are skipped. Compile regexes once, outside the file loop.
- **Generated HTML stays safe by default**: HTML-escape all names, URL-encode `href`s, keep the CSP meta tag (`default-src 'none'; style-src 'unsafe-inline'`), keep the default sensitive-file exclusions.
- **Handle `null`** from `File.listFiles()`/`list()` (unreadable directories).
- **Hot paths avoid intermediate string allocations** — single-pass escaping with lazy `StringBuilder`, direct hex-char mapping in URL encoding. Keep it that way when touching these functions.
- **Accessibility of generated HTML**: decorative icons wrapped in `<span aria-hidden="true">`, `aria-label`s on links, `<nav aria-label>` landmark, `prefers-reduced-motion` override.
- Existing function names use `snake_case` (`process_dir`, `write_index_file`); comments are mixed English/Korean. Follow the surrounding style.
