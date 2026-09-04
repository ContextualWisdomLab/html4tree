# Product / technical gap baseline

상태: Draft — protected integration 전 검증 중

## 제품 경계

html4tree는 디렉터리 내용을 정적 `index.html`로 표현하는 로컬 파일시스템 도구다. 파일 탐색 과정에서 사용자 제공 `.html4ignore`를 해석하지만, ignore 파일 자체나 파일시스템 경로의 변경 가능성을 신뢰 경계 밖 입력으로 취급한다.

## 현재 buyer-visible gap

`.html4ignore`가 일반 파일인지 확인한 직후 실제 open 전에 경로가 바뀔 수 있다. 사전 `isFile`/symlink 검사만으로는 이후 open 성공이나 동일 객체 유지를 보장할 수 없다. `NOFOLLOW_LINKS` open이 심볼릭 링크 교체를 거절하더라도 그 `IOException`이 상위 crawl까지 전파되면 공격자 또는 동시 파일 변경이 전체 인덱싱을 중단시키는 availability 결함이 된다.

반대로 open 이후의 임의 read/decoder 오류까지 모두 삼키면 실제 저장장치·스트림 오류를 정상 ignore-file 처리로 오인할 수 있다. 따라서 open 경쟁과 이미 열린 스트림의 실패를 같은 예외 경계로 취급하지 않는다.

## 결정

- production `process_ignore_file`은 `Files.newInputStream(path, READ, NOFOLLOW_LINKS)`로 최종 경로의 심볼릭 링크를 따라가지 않는다.
- 사전 검증 후 open 자체가 `IOException`으로 실패하면 해당 사용자 ignore 파일을 사용하지 않고 mandatory default exclusions를 계속 적용한다.
- stream이 성공적으로 열린 뒤 발생하는 read `IOException`은 숨기지 않고 전파한다. 이는 open-time TOCTOU/접근성 변화와 실제 read 실패를 구분하기 위한 의도적 경계다.
- test seam은 package-internal opener 함수로 한정한다. production public API와 실제 opener는 바꾸지 않는다.

기각한 대안:

- 사전 `isFile`/`isSymbolicLink` 검사만 신뢰: 검사와 사용 사이 경로가 바뀔 수 있어 기각한다.
- open/read 전체를 포괄적으로 `catch (Exception)` 처리: 실제 read 결함을 false-green으로 만들 수 있어 기각한다.
- 테스트에서 직접 `NOFOLLOW_LINKS`만 호출: 제품 함수의 fallback 계약을 검증하지 못해 기각한다.
- 권한 오류를 포함한 모든 symlink 생성 실패를 테스트 skip: 실행되지 않은 테스트를 성공으로 오인할 수 있어 기각한다.

## TDD / traceability

- Test-only RED lineage: `2a092932932d885158aa6156c101423081466fc2`는 정상 `.html4ignore` 사전조건을 만든 뒤 opener가 실패하는 결정적 계약과 post-open read 실패 전파 계약을 먼저 도입했다. 해당 시점에는 production seam이 없어 계약을 만족할 수 없었다. hosted RED 실행 결과는 아직 증거로 주장하지 않는다.
- Causal repair: `484946353e95351a4b3cce87ef26f502192cc365`는 public wrapper를 유지하면서 package-internal opener boundary를 추가하고 open-time `IOException`만 default-exclusion fallback으로 변환했다.
- 회귀 계약은 pre-existing symlink 미추적, check→open 실패 fallback, 정상 ignore pattern 적용, post-open read 실패 전파를 각각 관찰 가능한 결과로 검증한다.
- exact-head CI/SAST/Security/OSV/Scorecard가 모두 terminal GREEN이 되기 전에는 merge-ready 또는 release-ready로 간주하지 않는다. queued/pending/predecessor 결과는 승계하지 않는다.

## Standards / primary-source traceability

Oracle. (2026). *Files (Java SE 26 & JDK 26)*. Java Platform, Standard Edition 26 API Specification. https://docs.oracle.com/en/java/javase/26/docs/api/java.base/java/nio/file/Files.html

- `Files.newInputStream(Path, OpenOption...)`은 I/O 오류 시 `IOException`을 발생시킨다.
- `Files.exists(...)` 문서는 파일 존재 확인 결과가 즉시 오래될 수 있으며 이후 접근 성공을 보장하지 않는다고 명시한다. 같은 원칙상 security-sensitive path에서는 check 결과와 실제 use를 하나의 신뢰 가능한 상태로 간주하지 않는다.

Oracle. (2026). *LinkOption (Java SE 26 & JDK 26)*. Java Platform, Standard Edition 26 API Specification. https://docs.oracle.com/en/java/javase/26/docs/api/java.base/java/nio/file/LinkOption.html

- `NOFOLLOW_LINKS`는 심볼릭 링크를 따라가지 않는 파일 작업 옵션이다.

## 다음 acceptance

동일 exact contributor head에서 CI가 위 Kotlin 회귀를 실제 실행하고 JaCoCo 기준을 충족해야 한다. Security Scan, SAST, OSV, Scorecard도 같은 head에서 terminal success여야 하며, 새 substantive review finding이 있으면 다시 RED→causal fix→GREEN 순환으로 돌아간다. protected branch의 당시 live ruleset을 재조회한 뒤에만 normal merge를 판단한다.
