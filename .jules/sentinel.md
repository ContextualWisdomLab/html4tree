## 2026-06-24 - Cross-Site Scripting (XSS) vulnerability in directory/file names
**Vulnerability:** 디렉토리 이름과 파일 이름을 HTML 인덱스 파일에 삽입할 때 이스케이프 처리 없이 그대로 사용하여 Cross-Site Scripting (XSS) 취약점이 발생할 수 있습니다. 악의적인 이름을 가진 파일이나 디렉토리가 있을 경우 악성 스크립트가 실행될 수 있습니다.
**Learning:** 파일 시스템 내의 이름이라고 하더라도 항상 안전한 문자열이라고 가정하면 안 되며, 사용자의 입력이나 통제할 수 없는 데이터가 브라우저에 렌더링될 때는 반드시 보안 컨텍스트에 맞게 이스케이프해야 함을 확인했습니다.
**Prevention:** HTML에 삽입되는 모든 변수(디렉토리 이름, 파일 이름 등)에 대해 HTML 특수 문자를 이스케이프 처리하는 함수를 만들어 적용해야 합니다.
