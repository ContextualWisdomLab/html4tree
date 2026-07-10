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

## 2024-06-28 - [html4tree] 설정 파일에 대한 심볼릭 링크 및 디렉토리 공격
**Vulnerability:** 심볼릭 링크/디렉토리 형태의 `.html4ignore`를 통한 DoS 및 경로 탐색(Path Traversal) 취약점
**Learning:** 런타임에 파싱되는 애플리케이션 설정 파일(예: `.html4ignore`)의 파일 유형을 무조건적으로 신뢰할 경우 공격의 대상이 될 수 있습니다. 공격자가 `.html4ignore`라는 이름의 디렉토리를 생성하여 읽기 시 충돌을 유발하거나, `/dev/zero`나 `/dev/urandom`으로 심볼릭 링크를 연결하여 애플리케이션이 리소스를 무한정 소모하며 다운되도록(hang) 만들 수 있습니다.
**Prevention:** 설정 파일을 파싱하기 전에는 항상 해당 파일이 일반 파일인지(`isFile`) 확인하고 심볼릭 링크를 명시적으로 거부(`!Files.isSymbolicLink`)해야 합니다.
## 2024-06-29 - [html4tree] 민감한 디렉토리 순회 (정보 노출)
**Vulnerability:** 이전 보안 패치는 `process_dir()`에서 나열되는 디렉토리 목록에서 민감한 디렉토리를 제외하려 했습니다. 하지만 트리를 재귀적으로 순회하는 `go()` 함수가 제외 목록을 확인하지 않은 채 `Files.isDirectory`를 통해 하위 디렉토리를 무조건 탐색 큐에 삽입했습니다. 결과적으로 `.git`과 같은 민감한 디렉토리가 여전히 크롤링되어 내부에 `.git/index.html` 파일이 생성되었습니다.
**Learning:** 파일 목록에 대한 보안 제외 규칙은 트리 순회(Traversal) 단계에서도 동일하게 적용되어야 합니다. 상위 인덱스 파일에서 민감한 경로를 숨기더라도, 정적 생성기가 여전히 그 내부로 진입해 하위 인덱스를 생성한다면 정보 노출을 막을 수 없습니다.
**Prevention:** 재귀적 트리 순회 함수를 호출할 때 렌더링 함수와 완전히 동일한 제외 필터(예: `it.name !in exclude && !Files.isSymbolicLink(...)`)를 적용하여 안전하게 제어해야 합니다.
