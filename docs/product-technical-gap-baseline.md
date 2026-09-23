# Product / Technical Gap Baseline

이 문서는 `html4tree`의 제품 경계와 현재 구현의 검증 가능한 Gap을 코드와 같은 기준으로 관리한다. 추측성 severity나 아직 실행하지 않은 검증을 완료 증거로 취급하지 않는다.

## 제품 경계

`html4tree`는 로컬 디렉터리를 순회해 정적 `index.html` 목록을 생성하는 CLI다. 제품이 직접 소유하는 핵심 경계는 다음과 같다.

- 탐색 루트와 하위 디렉터리의 파일시스템 경계
- `.html4ignore` 정책 파일의 읽기·패턴 적용
- 민감 파일·숨김 파일의 기본 제외
- 파일명 HTML/URL 인코딩과 정적 페이지 CSP
- `index.html`의 임시 파일 작성·교체

외부 identity, LLM, database bounded context는 현재 제품 경계에 없다. 해당 foundation을 단순한 표준화 목적으로 강제 도입하지 않는다.

## Current implementation evidence

기준 PR은 #761이며 protected base는 `master@728f0f33323e43573d6664209891099502827d5d`다.

현재 branch lineage에서 `.html4ignore`는 `NOFOLLOW_LINKS`로 연 opened channel을 읽기 authority로 사용한다. channel의 reported size뿐 아니라 실제 read byte도 1 MiB에서 제한하고, UTF-8 decoder는 malformed/unmappable input을 fail-closed 한다. validation과 policy read 사이 directory-entry 교체를 재현하는 deterministic test와, 실제 read-byte overflow 및 malformed UTF-8 regression이 함께 존재한다.

`crawl_directories()`는 policy read가 `IgnoreFileReadException`으로 실패하면 해당 디렉터리의 index 생성과 child enqueue를 수행하지 않는다.

## Gap baseline

| Gap | 현재 상태 | 승격 조건 |
| --- | --- | --- |
| `.html4ignore` check/read pathname re-resolution | causal fix + deterministic regression 구현 | exact-head focused/full test와 security checks GREEN |
| policy file special-file / parent-directory replacement semantics | 부분 경계만 정의됨 | 지원 OS에서 secure-relative-open 또는 동등한 authority 모델을 정하고 deterministic hostile-case test 추가 |
| policy byte/line/pattern limits | 구현됨 | oversize reported size, actual read overflow, 1000-line boundary, malformed UTF-8 exact-head GREEN |
| directory identity TOCTOU | pre/post listing `fileKey` 검증 존재 | parent replacement 및 unsupported/null fileKey 플랫폼의 명시적 threat model과 테스트 |
| static HTML UI 접근성 | source-level keyboard/focus/empty-state 계약 일부 존재 | current-head browser E2E, narrow viewport, keyboard, screen-reader semantics evidence |
| 성능 주장 | 여러 in-code optimization 주석은 있으나 buyer workload 측정과 분리되어 있음 | 실제/right-cleared directory corpus에서 wall/CPU/allocation/GC median·p95 측정 후에만 성능 개선으로 승격 |
| release evidence | 이 PR은 Draft | protected-head merge 이후 version/CHANGELOG/tag/package/SBOM/provenance/reproducibility/rollback evidence가 같은 release에 연결되어야 함 |

## DDD / invariants

이 제품의 Core Domain은 **Directory Index Generation**이다. 현재 코드 수준에서 유효한 bounded-context 내부 invariant는 다음과 같다.

1. 크롤링 대상 directory identity가 traversal 도중 바뀌면 해당 entry를 처리하지 않는다.
2. `.html4ignore`가 존재하지만 안전하게 읽을 수 없으면 그 directory는 fail-closed 한다.
3. policy file bytes는 열린 handle에서만 authority를 얻으며, open 이후 pathname을 다시 resolve해 policy 내용을 읽지 않는다.
4. 민감·숨김 파일은 사용자 policy와 독립된 기본 제외 규칙을 가진다.
5. 파일명은 HTML text/attribute와 URL path에서 각각 해당 context에 맞게 encode한다.
6. `index.html` 교체는 atomic move를 우선하고, provider가 이를 지원하지 않는 경우에만 명시적 non-atomic replacement fallback을 사용한다.

이 invariant가 달라지는 PR은 production code, focused regression, 이 문서를 같은 lineage에서 갱신해야 한다.

## #761 acceptance

Ready 전 최소 증거는 다음과 같다.

- deterministic swap RED의 predecessor semantics가 설명 가능할 것
- single-open/no-follow fix에서 swap regression이 GREEN일 것
- reported-size 초과와 actual-read 초과가 모두 fail-closed 할 것
- malformed UTF-8, directory/symlink/broken-symlink/unreadable policy가 fail-closed 할 것
- normal glob, invalid glob, line/pattern limits와 기존 sensitive-file exclusions가 회귀하지 않을 것
- exact head의 CI, coverage, security, SAST/CodeQL 등 required checks가 terminal GREEN일 것

현재 PR이 Draft인 동안 이 문서는 release-ready를 주장하지 않는다.
