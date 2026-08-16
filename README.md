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
  --max-level INT  Number of levels deep for which to generate an index.html
                   file
  -h, --help       Show this message and exit

Arguments:
  TOPDIR  Top directory to crawl
```  

To run:

`$ java -jar ./build/libs/html4tree.jar <top directory to index>`

To only generate the index.html file for the top directory:

`$ java -jar ./build/libs/html4tree.jar <top directory to index> --max-level 0`

After upgrading, run the same command again on the live tree. New pages isolate
Arabic and Hebrew names so the Korean "파일" / "디렉토리" labels stay readable,
show each file's size and UTC last-modified time, and replace bidirectional
format controls in displayed names with a visible replacement character.
Open one generated `index.html` that contains a right-to-left name, and confirm
one file's size matches `wc -c` / `stat`, before you publish the tree. If a
row shows replacement characters, inspect the real name on disk before opening
the file.

## To exclude files from the generated index.html file

To exclude files, place a `.html4ignore` file in the directory. Each non-empty line is a Java file-system glob pattern matched against an entry name. Patterns longer than 100 characters and lines after the first 1,000 are ignored.

For example, to exclude files ending in `.txt`:

`*.txt`

## Other

To delete all the index.html files generated with one command, do:

`$ find <top directory to crawl> -name index.html -delete`

