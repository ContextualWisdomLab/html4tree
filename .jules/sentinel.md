## 2024-06-21 - [html4tree] Unsanitized Filenames in Auto-Generated HTML
**Vulnerability:** XSS via Malicious File/Directory Names
**Learning:** Tools that auto-generate static HTML pages from local file systems often overlook input sanitization, implicitly trusting local file paths. If these generated pages are hosted or shared, an attacker can create files with names like `<script>alert(1)</script>` to execute arbitrary JavaScript in the context of the user viewing the generated index.
**Prevention:** Always HTML-encode variable data injected into HTML templates, and URL-encode data used in `href` attributes, regardless of the data's origin (even if it's "just" the local file system). Additionally, ensure HTML attributes like `href` are properly quoted to prevent attribute breakout.

## 2023-10-25 - Path Traversal via Symlinks in Directory Crawlers
**Vulnerability:** The `html4tree` crawler was following symbolic links when generating its static HTML index, creating potential path traversal and arbitrary directory read vulnerabilities if links pointed outside the scope of the tree.
**Learning:** Checking `isDirectory()` is not enough in recursive tree walkers. In Kotlin/Java, a directory pointing to another part of the filesystem via symbolic link will pass `isDirectory()` checks but bypass the intended boundaries.
**Prevention:** Use `java.nio.file.Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)` for root and child directory checks, and skip symbolic links when rendering generated directory listings unless intentionally supporting external links with validation.

## 2024-06-25 - Prevent Directory Traversal through Symbolic Links
**Vulnerability:** Path traversal and arbitrary write via symlink parsing.
**Learning:** During directory traversal in `html4tree/main.kt`, `File.isDirectory()` returns `true` for symbolic links pointing to directories. A pre-existing `index.html` symlink can also redirect writes outside the intended tree.
**Prevention:** Skip symlinks during recursive directory processing and write generated HTML through a temporary file followed by an NIO move, so an `index.html` symlink is replaced rather than followed.

## 2024-06-26 - [Directory Traversal via Symlink / Un-writable DoS]
**Vulnerability:** The application could crawl symlink directories, accept a symlink as the top-level directory, and walk deeper than the requested max level before deciding not to render.
**Learning:** `File.listFiles()` returns null (not empty) on unreadable directories. Directory crawlers must reject symlink roots, skip symlink children, and avoid enqueueing paths deeper than the configured traversal limit.
**Prevention:** Use `java.nio.file.Files.isSymbolicLink(file.toPath())` for root and child directory checks, gracefully handle `null` arrays from `listFiles()` and `list()`, and only enqueue child directories when the current level is still below `maxLevel`.

## 2024-06-28 - [html4tree] Static HTML Generation Security
**Vulnerability:** Defense in Depth (CSP Missing)
**Learning:** Even when inputs are properly escaped, statically generated HTML that displays file/directory structures should implement a Content Security Policy (CSP) to provide an extra layer of defense against potential XSS bypasses.
**Prevention:** Include a strict CSP meta tag (e.g., `default-src 'none'; style-src 'unsafe-inline';`) in auto-generated HTML headers when external scripts or resources are not required.
## 2024-07-04 - [Symbolic Link Validation Bypass]
**Vulnerability:** 파일 경로를 검증할 때 `canonicalFile`을 사용하면 해당 경로가 심볼릭 링크인지 확인하기도 전에 실제 물리적 경로로 분석(resolving)되어버려, 이후 `LinkOption.NOFOLLOW_LINKS`를 이용한 심볼릭 링크 제한 로직이 우회되는 논리적 보안 취약점이 있었습니다.
**Learning:** `canonicalFile`은 운영 체제의 파일 시스템을 참고하여 모든 심볼릭 링크를 원본 경로로 치환합니다. 따라서 심볼릭 링크 여부를 검사하거나 링크 자체의 경로를 검증해야 할 때는 이를 사용하면 안 되며, 파일 시스템 조회 없이 경로 문자열만 정규화하는 `absoluteFile.toPath().normalize().toFile()` 방식을 사용해야 합니다.
**Prevention:** 심볼릭 링크에 대한 제약 조건(예: 디렉토리 탐색 금지)을 강제할 때는 경로를 해석(resolve)하는 함수(예: canonicalFile, realPath) 대신 단순 정규화 함수(normalize)를 사용하여 원본 경로 형태를 유지한 채 검증을 수행해야 합니다.
