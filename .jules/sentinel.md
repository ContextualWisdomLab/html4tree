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

## 2024-06-28 - [html4tree] 정적 HTML 생성 보안
**Vulnerability:** 심층 방어 누락 (CSP 누락)
**Learning:** 입력값이 적절히 이스케이프 되더라도, 파일/디렉토리 구조를 표시하는 정적 생성 HTML은 잠재적인 XSS 우회 공격에 대비하여 추가적인 방어 계층을 제공하는 콘텐츠 보안 정책(CSP)을 반드시 구현해야 합니다.
**Prevention:** 외부 스크립트나 리소스가 필요하지 않은 경우 자동 생성되는 HTML 헤더에 엄격한 CSP 메타 태그(예: `default-src 'none'; style-src 'unsafe-inline';`)를 포함하십시오.

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
## 2024-05-31 - [DoS Risk] Uncontrolled Resource Consumption in Ignore File Processing
**Vulnerability:** Processing `.html4ignore` reads regex patterns line-by-line without any upper limit on the number of patterns or their lengths. An attacker could craft a file with thousands of excessively long regex patterns causing a Denial of Service (DoS) and potentially ReDoS.
**Learning:** File processing loops, especially those dynamically compiling regular expressions, are vulnerable to uncontrolled resource consumption and regex denial of service. Memory limit exhaustion and high CPU loads are likely.
**Prevention:** Always impose sensible bounds (e.g., maximum line count, maximum pattern length) when dynamically processing loop-based inputs for resource-intensive operations like regex compilation.

## 2026-07-10 - [MEDIUM] ReDoS, OOM, and Root Crawl DoS Mitigations
**Vulnerability:** The `.html4ignore` parser still allowed excessively large files and root-directory crawls could generate unbounded filesystem output.
**Learning:** Local CLI configuration inputs and traversal roots need explicit resource ceilings, not only syntactic validation.
**Prevention:** Limit `.html4ignore` file size, parsed line count, compiled pattern count, and regex length; reject filesystem root traversal using `File.parentFile != null`.
## 2024-07-11 - [.html4ignore 파일의 ReDoS 취약점 수정 (Glob 패턴 적용)]
**Vulnerability:** [사용자가 제공한 `.html4ignore` 패턴을 직접 정규표현식으로 컴파일하여 발생하는 ReDoS(정규표현식 서비스 거부) 취약점 발견.]
**Learning:** [필터링 패턴으로 정규표현식을 직접 노출하면 악의적으로 조작된 긴 문자열이나 복잡한 패턴을 통해 애플리케이션의 리소스를 고갈시킬 수 있음.]
**Prevention:** [사용자 입력 패턴은 정규표현식으로 변환하기 전 `java.nio.file.FileSystems.getDefault().getPathMatcher("glob:$pattern")`와 같은 안전한 Glob 매칭 방식을 사용해야 함.]
## 2024-07-12 - [html4tree] Unhandled File/Directory Permissions causing DoS
**Vulnerability:** When encountering an unreadable `.html4ignore` file or a directory without write permissions, an unhandled `java.io.FileNotFoundException` or `java.nio.file.AccessDeniedException` was thrown, respectively. This causes the entire crawler to crash (DoS) when encountering files/directories with restricted permissions.
**Learning:** Application processes that recursively scan filesystem directories must gracefully handle permission denied exceptions to ensure that one inaccessible node does not halt the entire scanning/processing operation.
**Prevention:** Check `.canRead()` before attempting to parse configuration/ignore files, and wrap file writing operations in `try-catch` blocks to securely handle `AccessDeniedException` or other `IOException`s, failing gracefully (Fail Securely) rather than crashing the application.
