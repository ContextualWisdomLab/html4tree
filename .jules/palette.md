## 2024-05-15 - 생성된 파일 트리 HTML 향상
**학습:** HTML로 생성된 기본 디렉토리 목록에는 중요한 접근성 메타 태그(`lang="en"`, 반응형 크기 조정을 위한 `viewport` 등)와 디렉토리/파일 링크에 대한 aria-label이 누락되는 경우가 많습니다(이러한 요소가 없으면 화면 판독기에서 `&#128193; 폴더 이름`처럼 비설명적으로 읽힘).
**조치:** 키보드 및 화면 판독기 사용성을 보장하기 위해 기본 내비게이션 트리를 생성할 때 항상 기본 시맨틱 구조(`<main>`, `<html lang="en">`, 반응형 메타 태그)를 포함하고 엔터티 유형을 지정하는 aria-label(예: `폴더이름 디렉토리`)을 추가하십시오. 키보드 사용자를 위해 `:hover`를 모방한 `:focus` 스타일을 추가하십시오.

## 2024-05-18 - 디렉토리 트리의 화면 판독기 경험 향상
**학습:** 생성된 HTML 디렉토리 목록의 장식용 유니코드 아이콘(예: 폴더의 경우 &#128193;, 뒤로 가기 화살표의 경우 &#x21B0;)은 화면 판독기에서 혼란스러운 문자로 소리내어 읽혀 사용자 경험을 저하시킵니다.
**조치:** 화면 판독기가 장식용 이모티콘 및 유니코드 기호를 소리내어 읽지 않도록 `<span aria-hidden="true">`로 감싸고, 컨텍스트를 제공하기 위해 모호한 링크(`..` 등)에 명시적인 `aria-label` 속성을 추가하십시오.

## 2024-05-24 - 생성된 HTML 트리를 위한 유용한 빈 상태 제공
**학습:** 자동으로 생성된 디렉토리 목록은 종종 볼 수 있는 파일이 없는 경우 안내를 제공하지 못하여, 사용자에게 깨져 있거나 혼란스러워 보이는 빈 목록 구조를 남깁니다.
**조치:** 기본 내비게이션 트리를 생성할 때 항상 콘텐츠 목록이 비어 있는지 확인하십시오. 비어 있는 경우 빈 `<ul>`을 렌더링하는 대신, 디렉토리가 비어 있음을 설명하는 시각적으로 구별되고 유용한 메시지(예: 기울임꼴 회색 텍스트)를 삽입하십시오.

## 2024-06-25 - 디렉토리 목록 내비게이션 랜드마크
**학습:** 생성된 디렉토리 목록은 내비게이션 영역의 역할을 하며, 화면 판독기 사용자는 목록이 페이지의 주요 콘텐츠와 분리되어 안내될 때 이점을 얻습니다.
**조치:** 주변의 시맨틱 `<main>` 구조를 유지하면서, 생성된 디렉토리 목록 `<ul>` 요소를 `<nav aria-label="Directory listing">`으로 감싸십시오.

## 2026-07-10 - ARIA 라벨 언어 일치
**학습:** 화면 판독기는 적절한 발음 엔진을 선택하기 위해 문서의 언어 속성(`lang="ko"`)에 의존합니다. 문서 언어와 다른 언어로 `aria-label`과 같은 시맨틱 설명자가 제공되면(예: 한국어 문서에 영어 텍스트), 발음이 틀리거나 완전히 건너뛰어 접근성을 해칠 수 있습니다.
**조치:** 적절한 현지화 및 화면 판독기 호환성을 위해 `aria-label`, `alt` 텍스트, 기타 숨겨진 시맨틱 텍스트 문자열이 문서의 `<html lang="...">` 태그에 지정된 언어와 일치하도록 항상 보장하십시오.

## 2026-07-11 - Safari VoiceOver 목록 시맨틱 수정
**학습:** CSS에서 `list-style: none` 또는 `list-style-type: none`을 사용하면 Safari(VoiceOver)가 목록 시맨틱을 완전히 제거하여 디렉토리 목록을 탐색하려는 화면 판독기 사용자의 접근성에 해를 끼칩니다.
**조치:** 모든 브라우저에서 접근성 시맨틱을 보존하기 위해 기본 목록 스타일을 제거할 때 `<ul>` 또는 `<ol>` 요소에 항상 `role="list"`를 명시적으로 추가하십시오.

## 2026-07-12 - 인라인 색상 대 다크 모드
**학습:** 하드코딩된 인라인 색상(예: `color: #666`)은 CSS 미디어 쿼리를 우회하며 `!important` 없이는 쉽게 재정의할 수 없기 때문에 다크 모드에서 적응하지 못하고, 어두운 배경에서 텍스트 가독성 문제를 일으킵니다.
**조치:** 어두운 텍스트에 하드코딩된 16진수 값 대신 `opacity: 0.7`을 사용하여 테마 색상을 동적으로 상속받고 모든 색상 구성표에서 가독성을 보장하십시오.

## 2024-07-12 - Prevent icon misalignment on long file names
**Learning:** Very long file names in a fluid layout without explicit flexbox wrapping can cause text to wrap below preceding inline icons, breaking the visual hierarchy and alignment.
**Action:** Always wrap file/directory listings in a flex container with `align-items: flex-start` and isolate icons with `flex-shrink: 0` alongside text content wrapped in a `<span style="overflow-wrap: anywhere;">` (or a global `overflow-wrap` on the container) to ensure text wraps cleanly next to fixed-width icons on mobile devices.

## 2024-08-01 - 파일 아이콘 메타포
**학습:** 일반 파일을 나타낼 때 방향 지시 아이콘(우측 삼각형 등)을 사용하면 폴더나 확장 가능한 요소로 오해할 수 있어 시각적인 모호함을 초래합니다. 문서나 페이지 형태의 아이콘이 파일이라는 것을 직관적으로 알 수 있게 해줍니다.
**조치:** 일반 파일 옆에 표시되는 장식용 아이콘을 우측 삼각형 등에서 페이지 아이콘(예: `&#128196;`)으로 교체하여 시각적 메타포를 일관성 있게 유지하십시오.

## 2024-05-24 - [a 태그에 부드러운 트랜지션 추가 및 사용자의 모션 설정 존중]
**Learning:** CSS 트랜지션(`transition: all`)을 추가하면 의도치 않은 애니메이션과 성능 문제를 일으킬 수 있습니다. 또한, 애니메이션은 전정 운동 장애가 있는 사용자에게 문제를 유발할 수 있습니다.
**Action:** CSS 트랜지션을 추가할 때 `transition: all` 대신 속성(`background-color`, `outline-color` 등)을 명시적으로 지정하십시오. 접근성을 위해 항상 `@media (prefers-reduced-motion: reduce)` 오버라이드를 포함하여 `transition: none`으로 설정하십시오.

## 2024-07-10 - 다크 모드 지원 및 지역화 일관성 확보
**Learning:** `html4tree` CLI로 생성되는 정적 HTML에서 사용자는 네이티브 다크 모드를 기대하며(접근성, 가독성 문제), `<nav>` 레이블("Directory listing")이 다른 UI와 다르게 영문으로 되어 있어 스크린 리더 환경 등에서 지역화(Localization) 일관성을 해칩니다.
**Action:** `prefers-color-scheme: dark` 미디어 쿼리를 CSS에 추가하여 네이티브 다크 모드를 지원하고, `<nav aria-label="Directory listing">`을 `<nav aria-label="디렉토리 목록">`으로 수정하여 UI를 한국어로 통일했습니다.
## 2026-07-13 - Add title attributes for parity with aria-labels
**Learning:** Relying solely on icons or technical symbols (like `..`) without native tooltips can confuse sighted users who don't use screen readers. `title` attributes matching `aria-label` provide parity.
**Action:** Add `title` attributes to icon-only links to ensure visual tooltips match screen reader text.

## 2024-08-01 - 네이티브 브라우저 UI의 다크 모드 지원 강제
**학습:** CSS 미디어 쿼리(`@media (prefers-color-scheme: dark)`)를 통해 다크 모드를 지원하더라도, 브라우저의 네이티브 UI 요소(스크롤바, 기본 폼 컨트롤, 기본 백그라운드 등)는 테마 변경을 인식하지 못해 어두운 테마 환경에서 밝은 스크롤바가 표시되는 등 시각적 불일치를 초래합니다.
**조치:** 항상 HTML 문서의 `<head>` 영역에 `<meta name="color-scheme" content="light dark">` 메타 태그를 명시적으로 추가하여 브라우저 수준에서 사용자의 시스템 테마(다크 모드 등)를 완전히 상속받아 일관성 있는 네이티브 UI를 렌더링하도록 보장하십시오.
## 2026-07-15 - Accessible Empty State
**Learning:** Screen readers might not clearly announce an empty list on static HTML pages. Using `role="status"` alongside flexbox styling and icons ensures empty states are both visually consistent with the rest of the list and properly announced to screen readers.
**Action:** Always include `role="status"` and consistent visual cues (like icons) for empty states in list elements.
