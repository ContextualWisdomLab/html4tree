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

## 2024-05-31 - [CRITICAL] 심볼릭 링크 검사 우회 취약점 (canonicalFile)
**Vulnerability:** `File(path).canonicalFile`를 사용하여 심볼릭 링크 여부를 검사하면, `canonicalFile` 함수 내부에서 심볼릭 링크를 이미 대상(target) 파일의 실제 경로로 해석(resolve)해 버리기 때문에, 이후에 진행되는 `Files.isDirectory(..., LinkOption.NOFOLLOW_LINKS)` 등의 심볼릭 링크 제한 검사가 완전히 무력화되는 취약점이 발견되었습니다.
**Learning:** `canonicalFile`은 보안 검사(경로 조작 등)를 위해 절대 경로를 얻을 때 유용할 수 있지만, 심볼릭 링크 자체의 특성(심볼릭 링크인지 아닌지)을 보존해야 하는 맥락에서는 사용하면 안 됩니다.
**Prevention:** 심볼릭 링크 여부를 검사해야 하거나 심볼릭 링크 자체를 제한해야 하는 경우에는 `canonicalFile` 대신 `absoluteFile.toPath().normalize().toFile()`과 같이 심볼릭 링크를 해석하지 않고 경로만 정규화하는 방식을 사용해야 합니다.
## 2024-05-24 - [디렉토리 목록을 통한 정보 노출 (Information Exposure)]
**Vulnerability:** 정적 HTML 디렉토리 인덱서(`html4tree`)가 민감한 시스템 및 설정 파일(`.git`, `.env`, `.ssh`, `.htpasswd` 등)을 포함하여 모든 파일과 디렉토리를 무분별하게 나열하여, 생성된 HTML이 공개적으로 호스팅될 경우 심각한 정보 노출로 이어질 수 있었습니다.
**Learning:** 디렉토리 구조를 자동 생성하는 도구는 반드시 "기본적으로 안전한(secure by default)" 정책을 채택해야 합니다. 사용자 제공 무시 파일(`.html4ignore`)에만 의존하는 것은 사용자가 설정을 잊거나 어떤 파일이 민감한지 모를 수 있기 때문에 불충분합니다.
**Prevention:** 출력에서 기본적으로 제외되는 보편적으로 민감한 파일 및 디렉토리의 하드코딩된 기준 목록을 항상 포함하십시오. 이는 우발적인 정보 유출을 방지하기 위한 강력한 심층 방어(defense-in-depth) 조치로 작용합니다.

## 2024-06-28 - [html4tree] Symlink and Directory Attacks on Configuration Files
**Vulnerability:** DoS and Path Traversal via symlinked/directory `.html4ignore`
**Learning:** Application configuration files that are parsed at runtime (like `.html4ignore`) can be targeted if their file type is implicitly trusted. A user or attacker might create a directory named `.html4ignore` causing a crash upon reading, or symlink it to `/dev/zero` or `/dev/urandom` causing the application to hang and consume resources indefinitely.
**Prevention:** Always verify that configuration files are regular files (`isFile`) and explicitly reject symbolic links (`!Files.isSymbolicLink`) before attempting to parse them.
