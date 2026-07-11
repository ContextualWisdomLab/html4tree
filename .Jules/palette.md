## 2024-05-24 - [a 태그에 부드러운 트랜지션 추가 및 사용자의 모션 설정 존중]
**Learning:** CSS 트랜지션(`transition: all`)을 추가하면 의도치 않은 애니메이션과 성능 문제를 일으킬 수 있습니다. 또한, 애니메이션은 전정 운동 장애가 있는 사용자에게 문제를 유발할 수 있습니다.
**Action:** CSS 트랜지션을 추가할 때 `transition: all` 대신 속성(`background-color`, `outline-color` 등)을 명시적으로 지정하십시오. 접근성을 위해 항상 `@media (prefers-reduced-motion: reduce)` 오버라이드를 포함하여 `transition: none`으로 설정하십시오.
## 2024-07-10 - 다크 모드 지원 및 지역화 일관성 확보
**Learning:** `html4tree` CLI로 생성되는 정적 HTML에서 사용자는 네이티브 다크 모드를 기대하며(접근성, 가독성 문제), `<nav>` 레이블("Directory listing")이 다른 UI와 다르게 영문으로 되어 있어 스크린 리더 환경 등에서 지역화(Localization) 일관성을 해칩니다.
**Action:** `prefers-color-scheme: dark` 미디어 쿼리를 CSS에 추가하여 네이티브 다크 모드를 지원하고, `<nav aria-label="Directory listing">`을 `<nav aria-label="디렉토리 목록">`으로 수정하여 UI를 한국어로 통일했습니다.
