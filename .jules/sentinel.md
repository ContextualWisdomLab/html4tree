## 2024-06-21 - [html4tree] 자동 생성된 HTML 내 파일명 미정제
**Vulnerability:** 악의적인 파일/디렉토리 이름을 통한 XSS
**Learning:** 로컬 파일 시스템에서 정적 HTML 페이지를 자동 생성하는 도구는 종종 로컬 파일 경로를 암묵적으로 신뢰하여 입력값 정제(Sanitization)를 간과합니다. 생성된 페이지가 호스팅되거나 공유될 경우, 공격자는 `<script>alert(1)</script>`와 같은 이름의 파일을 생성하여 사용자가 생성된 인덱스를 볼 때 임의의 자바스크립트를 실행할 수 있습니다.
**Prevention:** 데이터 출처에 상관없이(로컬 파일 시스템이라 하더라도) HTML 템플릿에 삽입되는 변수 데이터는 항상 HTML 인코딩하고, `href` 속성에 사용되는 데이터는 URL 인코딩해야 합니다. 또한, 속성 탈출(Attribute Breakout)을 방지하기 위해 `href`와 같은 HTML 속성이 적절히 인용 부호로 처리되었는지 확인하십시오.

## 2023-10-25 - 디렉토리 크롤러에서 심볼릭 링크를 통한 경로 탐색(Path Traversal)
**Vulnerability:** `html4tree` 크롤러가 정적 HTML 인덱스를 생성할 때 심볼릭 링크를 따라가기 때문에, 링크가 트리의 범위를 벗어날 경우 잠재적인 경로 탐색 및 임의 디렉토리 읽기 취약점이 발생할 수 있었습니다.
**Learning:** 재귀적 트리 탐색기에서 `isDirectory()` 확인만으로는 충분하지 않습니다. Kotlin/Java에서 심볼릭 링크를 통해 파일 시스템의 다른 부분을 가리키는 디렉토리는 `isDirectory()` 검사를 통과하지만 의도된 경계를 우회합니다.
**Prevention:** 루트 및 하위 디렉토리 검사에 `java.nio.file.Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)`를 사용하고, 유효성 검사를 통해 외부 링크를 의도적으로 지원하지 않는 한 생성된 디렉토리 목록을 렌더링할 때 심볼릭 링크를 건너뛰십시오.

## 2024-06-25 - 심볼릭 링크를 통한 디렉토리 탐색 방지
**Vulnerability:** 심볼릭 링크 파싱을 통한 경로 탐색 및 임의 파일 쓰기.
**Learning:** `html4tree/main.kt`에서 디렉토리 탐색 중, 디렉토리를 가리키는 심볼릭 링크에 대해 `File.isDirectory()`는 `true`를 반환합니다. 기존에 존재하는 `index.html` 심볼릭 링크는 의도된 트리를 벗어나 파일을 쓰도록 리디렉션할 수 있습니다.
**Prevention:** 재귀적 디렉토리 처리 중 심볼릭 링크를 건너뛰고, NIO move를 사용하여 임시 파일을 통해 생성된 HTML을 기록하여, `index.html` 심볼릭 링크를 따라가지 않고 교체하도록 하십시오.

## 2024-06-26 - [심볼릭 링크를 통한 디렉토리 탐색 / 쓰기 불가 DoS]
**Vulnerability:** 애플리케이션이 심볼릭 링크 디렉토리를 크롤링하고, 심볼릭 링크를 최상위 디렉토리로 허용하며, 렌더링하지 않기로 결정하기 전에 요청된 최대 레벨보다 깊이 탐색할 수 있었습니다.
**Learning:** `File.listFiles()`는 읽을 수 없는 디렉토리에 대해 빈 배열이 아닌 `null`을 반환합니다. 디렉토리 크롤러는 심볼릭 링크 루트를 거부하고, 심볼릭 링크 자식을 건너뛰며, 구성된 탐색 제한보다 깊은 경로를 큐에 넣는 것을 피해야 합니다.
**Prevention:** 루트 및 자식 디렉토리 검사에 `java.nio.file.Files.isSymbolicLink(file.toPath())`를 사용하고, `listFiles()` 및 `list()`에서 반환되는 `null` 배열을 우아하게(gracefully) 처리하며, 현재 레벨이 여전히 `maxLevel` 미만일 때만 자식 디렉토리를 큐에 넣으십시오.

## 2024-06-28 - [html4tree] 정적 HTML 생성 보안
**Vulnerability:** 심층 방어 누락 (CSP 누락)
**Learning:** 입력값이 적절히 이스케이프 되더라도, 파일/디렉토리 구조를 표시하는 정적 생성 HTML은 잠재적인 XSS 우회 공격에 대비하여 추가적인 방어 계층을 제공하는 콘텐츠 보안 정책(CSP)을 반드시 구현해야 합니다.
**Prevention:** 외부 스크립트나 리소스가 필요하지 않은 경우 자동 생성되는 HTML 헤더에 엄격한 CSP 메타 태그(예: `default-src 'none'; style-src 'nonce-...'; base-uri 'none'; form-action 'none';`)를 포함하고, 스타일 블록에는 일회성 nonce를 부여하십시오.

## 2024-05-31 - [CRITICAL] 심볼릭 링크 검사 우회 취약점 (canonicalFile)
**Vulnerability:** `File(path).canonicalFile`를 사용하여 심볼릭 링크 여부를 검사하면, `canonicalFile` 함수 내부에서 심볼릭 링크를 이미 대상(target) 파일의 실제 경로로 해석(resolve)해 버리기 때문에, 이후에 진행되는 `Files.isDirectory(..., LinkOption.NOFOLLOW_LINKS)` 등의 심볼릭 링크 제한 검사가 완전히 무력화되는 취약점이 발견되었습니다.
**Learning:** `canonicalFile`은 보안 검사(경로 조작 등)를 위해 절대 경로를 얻을 때 유용할 수 있지만, 심볼릭 링크 자체의 특성(심볼릭 링크인지 아닌지)을 보존해야 하는 맥락에서는 사용하면 안 됩니다.
**Prevention:** 심볼릭 링크 여부를 검사해야 하거나 심볼릭 링크 자체를 제한해야 하는 경우에는 `canonicalFile` 대신 `absoluteFile.toPath().normalize().toFile()`과 같이 심볼릭 링크를 해석하지 않고 경로만 정규화하는 방식을 사용해야 합니다.

## 2024-05-24 - [디렉토리 목록을 통한 정보 노출 (Information Exposure)]
**Vulnerability:** 정적 HTML 디렉토리 인덱서(`html4tree`)가 민감한 시스템 및 설정 파일(`.git`, `.env`, `.ssh`, `.htpasswd` 등)을 포함하여 모든 파일과 디렉토리를 무분별하게 나열하여, 생성된 HTML이 공개적으로 호스팅될 경우 심각한 정보 노출로 이어질 수 있었습니다.
**Learning:** 디렉토리 구조를 자동 생성하는 도구는 반드시 "기본적으로 안전한(secure by default)" 정책을 채택해야 합니다. 사용자 제공 무시 파일(`.html4ignore`)에만 의존하는 것은 사용자가 설정을 잊거나 어떤 파일이 민감한지 모를 수 있기 때문에 불충분합니다.
**Prevention:** 출력에서 기본적으로 제외되는 보편적으로 민감한 파일 및 디렉토리의 하드코딩된 기준 목록을 항상 포함하십시오. 이는 우발적인 정보 유출을 방지하기 위한 강력한 심층 방어(defense-in-depth) 조치로 작용합니다.

## 2024-06-28 - [html4tree] 구성 파일에 대한 심볼릭 링크 및 디렉토리 공격
**Vulnerability:** 심볼릭 링크/디렉토리 `.html4ignore`를 통한 DoS 및 경로 탐색
**Learning:** 런타임에 파싱되는 애플리케이션 구성 파일(예: `.html4ignore`)은 해당 파일 유형이 암묵적으로 신뢰될 경우 표적이 될 수 있습니다. 사용자나 공격자는 `.html4ignore`라는 이름의 디렉토리를 생성하여 읽기 시 충돌을 유발하거나, `/dev/zero` 또는 `/dev/urandom`에 심볼릭 링크를 연결하여 애플리케이션이 멈추고 리소스를 무한히 소비하게 만들 수 있습니다.
**Prevention:** 구성 파일 파싱을 시도하기 전에 항상 구성 파일이 일반 파일인지(`isFile`) 확인하고 심볼릭 링크를 명시적으로 거부(`!Files.isSymbolicLink`)하십시오.

## 2024-05-31 - [DoS Risk] 무시 파일 처리 중 통제되지 않은 리소스 소비
**Vulnerability:** `.html4ignore` 처리가 패턴의 수나 길이에 대한 상한 없이 정규식 패턴을 줄 단위로 읽습니다. 공격자는 수천 개의 지나치게 긴 정규식 패턴이 포함된 파일을 조작하여 서비스 거부(DoS) 및 잠재적인 ReDoS를 유발할 수 있습니다.
**Learning:** 파일 처리 루프, 특히 정규식을 동적으로 컴파일하는 루프는 통제되지 않은 리소스 소비 및 정규식 서비스 거부 공격에 취약합니다. 메모리 한도 초과 및 높은 CPU 부하가 발생할 가능성이 높습니다.
**Prevention:** 정규식 컴파일과 같이 리소스 집약적인 작업을 위해 루프 기반 입력을 동적으로 처리할 때는 항상 합리적인 제한(예: 최대 줄 수, 최대 패턴 길이)을 부과하십시오.

## 2026-07-10 - [MEDIUM] ReDoS, OOM 및 루트 크롤링 DoS 완화
**Vulnerability:** `.html4ignore` 파서는 여전히 지나치게 큰 파일을 허용했으며, 루트 디렉토리 크롤링은 제한 없는 파일 시스템 출력을 생성할 수 있었습니다.
**Learning:** 로컬 CLI 구성 입력 및 탐색 루트에는 구문 유효성 검사뿐만 아니라 명시적인 리소스 상한이 필요합니다.
**Prevention:** `.html4ignore` 파일 크기, 구문 분석된 줄 수, 컴파일된 패턴 수 및 정규식 길이를 제한하고, `File.parentFile != null`을 사용하여 파일 시스템 루트 탐색을 거부하십시오.

## 2024-07-11 - [.html4ignore 파일의 ReDoS 취약점 수정 (Glob 패턴 적용)]
**Vulnerability:** 사용자가 제공한 `.html4ignore` 패턴을 직접 정규표현식으로 컴파일하여 발생하는 ReDoS(정규표현식 서비스 거부) 취약점 발견.
**Learning:** 필터링 패턴으로 정규표현식을 직접 노출하면 악의적으로 조작된 긴 문자열이나 복잡한 패턴을 통해 애플리케이션의 리소스를 고갈시킬 수 있음.
**Prevention:** 사용자 입력 패턴은 정규표현식으로 변환하기 전 `java.nio.file.FileSystems.getDefault().getPathMatcher("glob:$pattern")`와 같은 안전한 Glob 매칭 방식을 사용해야 함.

## 2024-07-12 - [html4tree] 처리되지 않은 파일/디렉토리 권한으로 인한 DoS
**Vulnerability:** 읽을 수 없는 `.html4ignore` 파일이나 쓰기 권한이 없는 디렉토리를 만났을 때 각각 처리되지 않은 `java.io.FileNotFoundException` 또는 `java.nio.file.AccessDeniedException`이 발생했습니다. 이로 인해 제한된 권한의 파일/디렉토리를 만날 때 전체 크롤러가 충돌(DoS)하게 됩니다.
**Learning:** 파일 시스템 디렉토리를 재귀적으로 검사하는 애플리케이션 프로세스는 접근할 수 없는 하나의 노드가 전체 스캔/처리 작업을 중단시키지 않도록 권한 거부 예외를 우아하게 처리해야 합니다.
**Prevention:** 구성/무시 파일을 파싱하기 전에 `.canRead()`를 확인하고, 파일 쓰기 작업을 `try-catch` 블록으로 감싸서 `AccessDeniedException` 또는 기타 `IOException`을 안전하게 처리하여 애플리케이션 충돌 대신 우아하게 실패(Fail Securely)하도록 하십시오.

## 2024-07-12 - [html4tree] TOCTOU (Time-of-Check to Time-of-Use) 심볼릭 링크 스왑 취약점 수정
**Vulnerability:** 큐(LinkedList)에 추가된 디렉토리가 대기하는 동안 외부(또는 공격자)에 의해 심볼릭 링크로 스왑될 수 있는 TOCTOU 취약점.
**Learning:** 큐에 넣기 전(`listFiles`)에 한 번 검사했다고 해서, 큐에서 빼내어 처리(`process_dir`)하는 시점에도 파일 시스템 상태가 동일할 것이라고 가정(Implicit Trust)하면 안 됩니다.
**Prevention:** 큐에 넣는 시점(`Time-of-Check`)에 파일의 고유 식별자(`BasicFileAttributes.fileKey()`)를 캡처해두고, 큐에서 꺼내어 실제로 처리하는 시점(`Time-of-Use`)에 현재 파일의 `fileKey()`를 다시 읽어 두 값이 일치하는지 재검증(Re-verify)해야 합니다.

## 2024-05-25 - Information Exposure via Default Inclusion and Referrer
**Vulnerability:** Common sensitive files (like `.aws`, `.kube`, `.npmrc`) could be accidentally indexed if present in the tree. Furthermore, clicking on external links (if any were added) could leak the directory structure via the HTTP Referer header.
**Learning:** Default exclude lists must encompass modern toolchains and cloud credentials, as users often run directory indexers in their home or project root directories. HTML templates need explicit policies to prevent accidental data leakage via headers.
**Prevention:** Maintain an extensive default deny-list for known sensitive files and enforce `no-referrer` globally on generated index pages.

## 2024-07-07 - [Sensitive Data Exposure in Directory Indexing]
**Vulnerability:** The application was traversing and listing hidden files and directories (those starting with `.`), potentially exposing sensitive information like `.git` histories or `.env` configuration files in the generated HTML index.
**Learning:** This existed because the traversal and filtering logic did not explicitly account for standard conventions regarding hidden files, defaulting to listing everything not explicitly ignored.
**Prevention:** Always implement explicit filters for hidden files and directories (e.g., `!file.name.startsWith(".")`) in applications that generate static files or expose directory structures to the public.


## 2024-05-18 - Prevent Sensitive Information Disclosure
**Vulnerability:** The application lists all files in a directory, including hidden files (those starting with `.`), which could inadvertently expose sensitive information like `.env`, `.git`, or `.ssh` directories.
**Learning:** Default directory listing implementations without hidden file filtering can lead to information disclosure vulnerabilities when serving directories containing configuration or sensitive files.
**Prevention:** Automatically exclude hidden files (files starting with `.`) from the generated directory listing by default.

## 2024-07-13 - [MEDIUM] 정적 리소스용 CSP 생성 방식(Nonce 대신 해시) 관련 보안 강화
**Vulnerability:** 정적 HTML 생성 도구에서 매번 다른 Nonce를 동적으로 생성하여 CSP에 적용하는 것은, 캐싱 효율을 저하시킬 뿐만 아니라 정적 배포 환경(예: GitHub Pages 등)에서 올바른 보안 정책 수립을 방해할 수 있는 안티 패턴입니다.
**Learning:** 정적으로 고정된 인라인 스타일이나 스크립트에는 난수화된 Nonce보다 콘텐츠 자체의 해시(SHA-256 등)를 사용하는 것이 안전하고 일관된 방식임을 배웠습니다.
**Prevention:** 자동 생성되는 정적 HTML의 콘텐츠 보안 정책(CSP)에는 `style-src 'sha256-<HASH>'` 방식을 적용하고, `<style>` 태그에서 불필요한 `nonce` 속성을 제거하여 브라우저의 무결성 검증 기능을 적극 활용하십시오.

## 2024-07-17 - [html4tree] 원자적 파일 이동을 통한 보안 및 안정성 강화
**Vulnerability:** 기존 `write_index_file`은 임시 파일을 `REPLACE_EXISTING`만 사용하여 대상 위치로 이동시켰습니다. 이는 파일 교체 중 불완전한 상태(부분 읽기)가 노출되거나, 교체 직전 대상 경로가 악의적인 심볼릭 링크로 변경되는 TOCTOU(Time-of-Check to Time-of-Use) 공격에 취약할 수 있습니다.
**Learning:** 파일 교체 작업에서 원자성(Atomicity)을 보장하는 것은 데이터 손상 방지 및 동시성 관련 보안 문제를 예방하는 핵심 요소입니다. Java/Kotlin에서 파일 시스템이 지원하는 경우 `StandardCopyOption.ATOMIC_MOVE`를 사용하면 이러한 위험을 줄일 수 있습니다.
**Prevention:** 정적 파일이나 구성 파일을 작성할 때는 임시 파일을 먼저 작성한 후, `StandardCopyOption.ATOMIC_MOVE`와 `REPLACE_EXISTING`을 함께 사용하여 대상 파일로 이동시킵니다. 대상 파일 시스템에서 원자적 이동을 지원하지 않는 경우(`AtomicMoveNotSupportedException` 발생)에 대비해 `REPLACE_EXISTING`만 사용하는 안전한 폴백(fallback) 메커니즘을 구현하십시오.
