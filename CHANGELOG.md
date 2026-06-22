# 변경 사항

- `html4tree`의 단위 테스트 (`Html4treeTest.kt`)를 추가하여, 모든 클래스와 메소드에 대한 테스트 수행.
- `LinkedList`, `go()`, `process_ignore_file()`, `process_dir()`, `main()`, `help()` 등 모든 함수에 대한 커버리지를 100%로 달성.
- `jacoco` 플러그인을 `build.gradle`에 추가하여, `gradlew test jacocoTestReport`를 통해 코드 커버리지 보고서가 생성되도록 수정.
- 사용되지 않고 도달할 수 없는 코드를 제거하여 분기 커버리지 100% 달성.
    - `while(lle != null && lle.file.isDirectory())`에서 `isDirectory()`는 `ll.push` 시 이미 체크하므로 항상 참이어서 도달할 수 없는 분기를 발생시켜, `while(lle != null)`로 수정.
    - `if((it.getName() !in exclude) && (it != curr_dir))`에서 자바의 `listFiles()`는 자기 자신(디렉토리 자체)을 리턴하지 않기 때문에 항상 참이 되어 도달할 수 없는 분기를 발생시켜, `if(it.getName() !in exclude)`로 수정.
