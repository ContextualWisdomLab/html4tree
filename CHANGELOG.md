# 변경 사항 (CHANGELOG)

## 보안 수정
- `index.html` 파일을 생성할 때 심볼릭 링크(symlink)를 통해 악의적으로 임의의 파일이 덮어씌어지는 취약점(Arbitrary File Write) 수정. `process_dir` 로직에 대상 파일이 심볼릭 링크일 경우 이를 안전하게 삭제하고 새 파일로 교체하는 방어 코드 추가.
