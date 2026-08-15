html4tree
=========

## Description

This program generates index.html files based on a file directory tree.
Think Apache mod_autoindex.
See [https://httpd.apache.org/docs/2.4/mod/mod_autoindex.html](https://httpd.apache.org/docs/2.4/mod/mod_autoindex.html).
It is written in Kotlin.

## License 

html4tree is copyrighted free software by Yamir Encarnación &lt;yencarnacion@webninjapr.com&gt;. You can redistribute it and/or modify it under the terms of the MIT license(see the file LICENSE).

## How to compile

To compile:

`$ ./gradlew`

## How to run

To obtain help:

```
java -jar ./build/libs/html4tree.jar -h
Usage: html4tree [OPTIONS] TOPDIR

Options:
  --max-level INT     Number of levels deep for which to generate an
                      index.html file
  --force-overwrite   Destructively replace an unmarked existing index.html.
                      Symlinks and directories are still refused.
  --cleanup           Delete only html4tree-owned index.html files under
                      TOPDIR. Unowned files are preserved.
  --dry-run           With --cleanup, report owned artifacts that would be
                      deleted without deleting them.
  -h, --help          Show this message and exit

Arguments:
  TOPDIR  Top directory to crawl
```  

To run:

`$ java -jar ./build/libs/html4tree.jar <top directory to index>`

To only generate the index.html file for the top directory:

`$ java -jar ./build/libs/html4tree.jar <top directory to index> --max-level 0`

## To exclude files from the generated index.html file

To exclude files, place a `.html4ignore` file in the directory. Each non-empty line is a Java file-system glob pattern matched against an entry name. Patterns longer than 100 characters and lines after the first 1,000 are ignored.

For example, to exclude files ending in `.txt`:

`*.txt`

## Generated index ownership

html4tree owns only the files it generated. Every new `index.html` includes:

```html
<meta name="generator" content="html4tree/1">
```

- A missing `index.html` is created.
- An existing file is replaced only when that marker is present and supported.
- A user-authored, malformed, unsupported, or late-marker `index.html` is left untouched.
- Symbolic links and directories occupying `index.html` are never replaced, even with `--force-overwrite`.
- `--force-overwrite` is an explicit, destructive opt-in for unmarked regular files.
- `--dry-run` is valid only with `--cleanup`; using it by itself is rejected before generation starts.

Do not run `find ... -name index.html -delete`. That command cannot tell generated pages from customer home pages.

To preview or remove only html4tree-owned pages:

```bash
java -jar ./build/libs/html4tree.jar --cleanup --dry-run <top directory to crawl>
java -jar ./build/libs/html4tree.jar --cleanup <top directory to crawl>
```

Pre-marker pages from older html4tree versions are treated as unowned. Do not guess ownership from arbitrary HTML. If you must replace a known unmarked page, use `--force-overwrite` on that tree after a backup.

If publication and automatic restoration both fail, html4tree keeps the same-directory `.index-owned-backup-*` recovery file and reports its exact path. Recover that file before rerunning cleanup or generation.

See `docs/doctoring/generated-index-ownership.md`.

