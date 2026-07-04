# 변경 사항

- `html4tree/main.kt`의 루트 디렉터리(`topDir`) 심볼릭 링크 검사 우회 취약점 수정. `File.canonicalFile`이 심볼릭 링크를 자동 해석하여 `LinkOption.NOFOLLOW_LINKS` 검사가 무력화되는 문제를 방지하기 위해 `File.absoluteFile`로 변경.
