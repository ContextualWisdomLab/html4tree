# CHANGELOG

## [Unreleased]

### 보안 (Security)
- `index.html` 파일을 생성할 때 파일 및 디렉토리 이름이 렌더링되는 부분에 발생할 수 있는 크로스 사이트 스크립팅(XSS) 취약점을 수정했습니다.
- HTML 이스케이프 함수(`escapeHtml`) 및 URL 인코딩 함수(`urlEncode`)를 도입하여 악의적인 스크립트 실행과 경로 문제를 방지했습니다.