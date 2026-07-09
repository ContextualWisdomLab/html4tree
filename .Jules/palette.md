## 2024-05-24 - [Add smooth transitions for a tags but respect user motion preferences]
**Learning:** Adding CSS transitions (`transition: all`) can create unintended animations and performance issues. Also, animations can cause problems for users with vestibular motion disorders.
**Action:** When adding CSS transitions, explicitly specify properties (e.g., `background-color`, `outline-color`) instead of `transition: all`. Always include a `@media (prefers-reduced-motion: reduce)` override to set `transition: none` for accessibility.
## 2026-07-09 - [다크 모드 지원 및 접근성 향상, CSP 준수를 위한 인라인 스타일 제거]
**Learning:** `Content-Security-Policy`가 `style-src 'unsafe-inline'`를 허용하더라도, 유지보수성과 보안을 높이기 위해 인라인 스타일 대신 CSS 클래스를 활용하는 것이 낫다. 특히, 정적 HTML 생성 과정에서 하드코딩된 영문 접근성 속성(`aria-label="Directory listing"`)이 한국어 환경에서 스크린 리더 사용자에게 어색할 수 있으므로 이를 언어에 맞게 번역하는 것이 중요하다. 또한 정적 파일 트리 렌더링에 다크 모드(`@media (prefers-color-scheme: dark)`)를 지원하면 눈부심을 줄이고 전반적인 UX 만족도를 크게 향상시킬 수 있다.
**Action:** 앞으로 HTML 구조를 동적으로 생성할 때, 인라인 스타일을 배제하고 CSS 클래스 기반의 디자인을 채택하여 CSP와의 충돌 가능성을 줄인다. 추가로 `aria-label` 같은 접근성 속성은 템플릿 언어에 알맞게 번역되었는지 확인하며, 스타일 시트를 삽입할 때 다크 모드 미디어 쿼리를 기본으로 추가하는 것을 잊지 않는다.
