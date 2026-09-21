import re

with open("src/main/kotlin/html4tree/main.kt", "r") as f:
    main_kt = f.read()

old_ignore_check = """    // 보안 향상: .html4ignore 파일이 일반 파일인지 확인하고, 심볼릭 링크인 경우 무시하여 DoS 및 경로 조작을 방지합니다.
    // 보안 향상: 파일 크기(1MB 제한) 및 줄 수(1000줄), 정규식 길이(100자)를 제한하여 ReDoS 및 메모리 고갈(OOM) 방지
    // 보안 향상: 권한이 없는 파일 접근 시 발생하는 예외(DoS)를 방지하기 위해 canRead() 추가 확인
    if (ignore_file.exists()) {
        if (!ignore_file.isFile || Files.isSymbolicLink(ignore_file.toPath()) || !ignore_file.canRead() || ignore_file.length() > 1048576) {
            throw IgnoreFileReadException("Policy file is inaccessible, unsafe, or too large")
        }
        val ignored_matchers = mutableListOf<java.nio.file.PathMatcher>()"""

new_ignore_check = """    // 보안 향상: .html4ignore 파일이 일반 파일인지 확인하고, 심볼릭 링크인 경우 무시하여 DoS 및 경로 조작을 방지합니다.
    // 보안 향상: 파일 크기(1MB 제한) 및 줄 수(1000줄), 정규식 길이(100자)를 제한하여 ReDoS 및 메모리 고갈(OOM) 방지
    // 보안 향상: 권한이 없는 파일 접근 시 발생하는 예외(DoS)를 방지하기 위해 canRead() 추가 확인
    if (Files.exists(ignore_file.toPath(), LinkOption.NOFOLLOW_LINKS)) {
        if (!ignore_file.isFile || Files.isSymbolicLink(ignore_file.toPath()) || !ignore_file.canRead() || ignore_file.length() > 1048576) {
            throw IgnoreFileReadException("Policy file is inaccessible, unsafe, or too large")
        }
        val ignored_matchers = mutableListOf<java.nio.file.PathMatcher>()"""

main_kt = main_kt.replace(old_ignore_check, new_ignore_check)

with open("src/main/kotlin/html4tree/main.kt", "w") as f:
    f.write(main_kt)
