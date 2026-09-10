# Product / Technical Gap Baseline

`html4tree`는 파일 시스템 트리를 정적 HTML directory index로 생성한다. 파일·디렉터리 이름은 product input이며, HTML escaping·URL encoding·filesystem boundary와 별도로 **표시 방향성**도 사용자에게 보이는 계약이다.

## BiDi 표시 계약 — PR #665

문제는 방향을 미리 알 수 없는 파일·디렉터리 이름이 주변 한국어 UI와 같은 directional context에 섞일 때 숫자·구두점·RTL/LTR run의 시각적 순서가 주변 label까지 영향을 줄 수 있다는 점이다.

선택한 구현은 W3C Internationalization guidance와 맞춘다.

- markup으로 감쌀 수 있는 directory heading과 file-name span에는 `dir="auto"`를 사용한다.
- markup을 넣을 수 없는 `<title>`과 `title` attribute의 사용자 제어 phrase에는 FSI(U+2068) / PDI(U+2069)를 사용한다.
- HTML escaping과 URL encoding은 기존 보안 경계를 그대로 유지한다.

이 선택은 **주변 문맥과 사용자 제어 phrase의 방향성을 격리**하는 것이다. 파일명 자체에 들어 있는 RLO/LRO 등 explicit override를 제거하거나, 파일명 내부의 모든 시각적 spoofing을 방지하는 security sanitizer로 간주하지 않는다. 따라서 별도 PR #664의 일반적인 “BiDi spoofing 취약점 수정” 서술은 기각하고, byte-identical production/test delta는 #665로 승계한다.

참고: W3C, *Inline markup and bidirectional text in HTML*, https://www.w3.org/International/articles/inline-bidi-markup/index.en.html

## 완료 전 Gap

현재 unit test는 생성 HTML string에 `dir="auto"`와 FSI/PDI가 들어가는 계약을 확인하지만 material UI acceptance를 대체하지 않는다. Ready 전에는 current-head에서 실제 mixed LTR/RTL filename을 사용해 다음을 검증한다.

- heading/file row와 surrounding Korean label의 방향 보존
- page title과 tooltip의 isolate 동작
- keyboard navigation 및 accessibility tree에서 link name/type 정보 보존
- normal/empty/error 상태와 narrow/intermediate/desktop viewport의 clipping·overflow 여부
- current-head screenshot/E2E와 동일 generation의 CI/Security/SAST/CodeQL, qualifying review

Delivery Gate: 의도성 PASS / 기능 완전성 PARTIAL / 콘텐츠 적합성 PASS / 복원력 PENDING / 증거성 FAIL·PENDING / 고유성 N/A
