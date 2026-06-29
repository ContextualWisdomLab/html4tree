# 변경 사항 (Changelog)

## [Unreleased]

### 보안 (Security)
- 생성되는 HTML의 `<head>` 태그 내에 `<meta charset="utf-8">` 및 Content-Security-Policy (CSP) 메타 태그를 추가하여 교차 사이트 스크립팅(XSS) 및 UTF-7 다운그레이드 공격에 대한 방어 계층을 추가했습니다.
- 모든 파일에 대한 단위 테스트를 추가하고 JaCoCo를 설정하여 테스트 커버리지 100%를 달성했습니다.
