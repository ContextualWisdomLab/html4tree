# Product–technical gap baseline

기준일: 2026-09-10

html4tree의 핵심 구매 가치 중 하나는 디렉터리의 실제 공개 정책을 `index.html`에 정확히 반영하는 것입니다. 따라서 `.html4ignore`는 단순 설정 파일이 아니라 Directory Publication bounded context의 정책 입력이며, 읽기에 실패하거나 읽는 대상의 identity가 검증 시점과 달라진 경우에는 공개를 계속하지 않는 쪽이 안전한 기본값입니다.

## 현재 경계

- **Subdomain / Bounded Context:** Directory Publication
- **Policy boundary:** `.html4ignore`를 읽어 현재 디렉터리에서 공개하지 않을 entry 집합을 계산합니다.
- **Invariant:** directory snapshot에 `.html4ignore`가 존재한다고 관찰한 경우, 그 정책을 안전하게 읽지 못하면 해당 directory의 index를 생성하지 않습니다.
- **Failure semantics:** unreadable file, directory, symbolic link, oversized policy 또는 read I/O failure는 빈 정책으로 대체하지 않고 `IgnoreFileReadException`으로 fail closed 합니다.
- **Publication boundary:** `crawl_directories`와 `process_dir`는 정책 읽기 실패가 발생한 directory를 게시하지 않습니다.

## 2026-09-10 verified gap — check/use 사이의 symlink 교체

PR #667의 선행 구현은 `isFile`, `isSymbolicLink`, `canRead`, `length`를 pathname으로 확인한 뒤 일반 `File.useLines`로 다시 pathname을 열었습니다. 검증 직후 `.html4ignore`를 symbolic link로 교체하면 마지막 open이 link를 따라갈 수 있으므로, "TOCTOU를 수정했다"고 일반화하기에는 보호 경계가 부족했습니다. CWE-367의 핵심도 검증한 속성이 실제 사용 전에 변경될 수 있다는 점입니다.

TDD regression은 metadata 검증이 끝난 직후 `.html4ignore`를 symbolic link로 교체합니다. test-only commit `34f4e59431ce960139731c9c44d5a2fe61aebbee`에서는 기존 reader가 link를 따라가므로 fail-closed 계약을 만족하지 못합니다. source-fix ancestor `53e6f0c812910a60dc3158928ae84dbfc7a2f321`에서는 package-local policy reader가 `Files.newByteChannel(..., READ, NOFOLLOW_LINKS)`로 최종 path component를 열고, 1 MiB 제한도 pathname 재조회가 아니라 열린 channel의 `size()`로 확인합니다. link 교체가 일어나면 read가 실패하여 기존 `IgnoreFileReadException` 경계로 수렴합니다.

이 조치는 **최종 `.html4ignore` component의 symlink-follow race와 descriptor 밖 size check**를 좁혀 고친 것입니다. 상위 디렉터리 자체가 교체되는 모든 형태의 pathname race, 공격자가 같은 regular policy file의 내용을 합법적으로 다시 쓰는 경우, 모든 filesystem/provider에서의 동일한 atomicity를 해결했다고 주장하지 않습니다. 그런 위협까지 요구되는 배포에서는 open directory handle에 상대적인 `SecureDirectoryStream.newByteChannel(..., NOFOLLOW_LINKS)` 또는 OS별 openat/openat2 계열 primitive를 사용하는 별도 owner decision이 필요합니다.

## 선택과 기각

선택한 최소 수리는 기존 `process_ignore_file`의 parsing·glob·default-sensitive-file 계약을 바꾸지 않고 실제 read open만 `NOFOLLOW_LINKS`로 강화하는 것입니다. 열린 channel에서 size를 검사해 check/use 창도 하나 줄였습니다.

기존 `isSymbolicLink` 확인만 유지하는 안은 check와 open 사이 교체를 막지 못해 기각했습니다. 반대로 이번 PR에서 crawler 전체를 platform-specific native `openat2` 구현으로 교체하는 안은 현재 Kotlin CLI의 범위를 크게 넓히고 portability 결정을 동반하므로 별도 architecture/security decision 없이 섞지 않습니다.

## Acceptance

PR #667이 Ready 또는 mergeable로 승격되려면 동일 exact head에서 다음이 모두 성립해야 합니다.

1. metadata 검증 뒤 `.html4ignore`를 symbolic link로 교체하는 regression이 `IgnoreFileReadException`으로 GREEN이어야 합니다.
2. 기존 unreadable/directory/symlink/oversized/malformed-pattern/default-sensitive-file regression을 보존해야 합니다.
3. Gradle test와 JaCoCo 100% instruction gate, repository Security Scan, SAST, CodeQL이 terminal GREEN이어야 합니다.
4. PR 설명은 "모든 TOCTOU 취약점 제거"가 아니라 실제로 검증한 fail-closed 및 final-component symlink-open 경계를 기술해야 합니다.
5. 상위 디렉터리 identity까지 공격자 변경 가능 경계로 둘 것인지 결정할 경우, `SecureDirectoryStream` 지원/미지원 provider의 fail-closed 정책과 portability를 ADR로 분리합니다.

## Traceability

- MITRE. (n.d.). *CWE-367: Time-of-check time-of-use (TOCTOU) race condition*. CWE. https://cwe.mitre.org/data/definitions/367.html
- Oracle. (n.d.). *LinkOption*. Java Platform, Standard Edition 21 API Specification. https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/file/LinkOption.html
- Oracle. (n.d.). *SecureDirectoryStream*. Java Platform, Standard Edition 21 API Specification. https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/file/SecureDirectoryStream.html
