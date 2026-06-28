## 2024-10-25 - 디렉토리 뷰어의 접근성 향상 (Accessibility improvement in Directory Viewer)
**Learning:** 장식용 아이콘(예: 폴더 이모지나 화살표)이 스크린 리더에서 읽히면 시각 장애인에게 불필요한 혼란(노이즈)을 줄 수 있으며, 목록을 탐색할 때 랜드마크(main)와 접근성 이름(aria-label)이 있는 네비게이션 요소(nav)를 제공해야 전체 구조를 이해하기 쉽습니다.
**Action:** 장식용 문자는 반드시 `<span aria-hidden="true">`로 감싸고, 주요 탐색 요소는 시맨틱 태그(nav, main)와 적절한 `aria-label`을 사용하여 묶는 패턴을 표준으로 적용합니다.
