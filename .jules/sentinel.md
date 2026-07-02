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

## 2026-07-02 - [Fix Path Traversal/Symlink Bypass in File Checking]
**Vulnerability:** 심볼릭 링크 디렉토리인지 검증하는 과정에서 `File.canonicalFile`을 사용하면, JVM/OS가 심볼릭 링크를 대상 경로(실제 디렉토리)로 미리 해석(resolve)해 버립니다. 그 결과 `Files.isDirectory(..., LinkOption.NOFOLLOW_LINKS)`가 심볼릭 링크 자체가 아닌 실제 디렉토리를 검사하게 되어 심볼릭 링크 검증을 우회하는 보안 취약점이 발생했습니다.
**Learning:** `canonicalFile`은 모든 심볼릭 링크를 해석하고 정규화된 절대 경로를 반환합니다. 심볼릭 링크 자체의 속성을 검사해야 할 때(예: 심볼릭 링크를 허용하지 않거나, 경로 탐색을 방지해야 하는 경우) `canonicalFile`을 사용하면 검증 로직이 무효화될 수 있습니다.
**Prevention:** 심볼릭 링크 검증 시에는 경로의 상대성만 해결하고 심볼릭 링크를 해석하지 않는 `absoluteFile`을 사용해야 합니다. 그 후 `LinkOption.NOFOLLOW_LINKS`와 같은 옵션을 사용하여 안전하게 파일 시스템 속성을 검사해야 합니다.
