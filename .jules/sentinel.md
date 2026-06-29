## 2023-10-25 - XSS 및 URL 인코딩 취약점 수정 (Sentinel)
**Vulnerability:** `html4tree`의 `process_dir` 로직에서 디렉토리 및 파일명을 이스케이프 및 인코딩 없이 직접 HTML의 링크와 타이틀, `href` 속성에 바인딩하여 XSS 공격 및 띄어쓰기가 있는 파일명 접근 에러(URL Path 깨짐)가 발생할 수 있었습니다.
**Learning:** 단순한 `String` 이어붙이기 방식으로 HTML을 렌더링할 때는 사용자 입력이나 외부 상태(이 경우 파일 시스템 상의 파일 이름)에 의해 브라우저가 악의적인 스크립트를 파싱하게 될 위험성이 존재한다는 것을 이 코드베이스에서 확인할 수 있었습니다. 특히 Kotlin의 raw string literal(`"""`)을 사용할 때도 변수 삽입 부분은 철저히 필터링되어야 합니다.
**Prevention:** `String.escapeHtml()`과 `String.encodeUrlPath()`라는 확장 함수를 만들어 HTML 엔티티 치환(특히 `<`, `>`, `&`, `"`, `'`)과 `java.net.URLEncoder.encode`를 결합하여 안전하게 출력하도록 수정했습니다. 나아가, 추후 유사한 HTML 템플릿 렌더링 시에는 파일명과 같이 통제 불가능한 문자열에 대해서는 항상 이스케이프 함수를 거치도록 해야 합니다.
