## 2024-06-21 - [html4tree] Unsanitized Filenames in Auto-Generated HTML
**Vulnerability:** XSS via Malicious File/Directory Names
**Learning:** Tools that auto-generate static HTML pages from local file systems often overlook input sanitization, implicitly trusting local file paths. If these generated pages are hosted or shared, an attacker can create files with names like `<script>alert(1)</script>` to execute arbitrary JavaScript in the context of the user viewing the generated index.
**Prevention:** Always HTML-encode variable data injected into HTML templates, and URL-encode data used in `href` attributes, regardless of the data's origin (even if it's "just" the local file system). Additionally, ensure HTML attributes like `href` are properly quoted to prevent attribute breakout.

## 2024-06-29 - Arbitrary File Write via Symlink in html4tree
**Vulnerability:** index.html 생성 시 기존의 심볼릭 링크를 따라가 원본 파일을 덮어쓰는 취약점(Arbitrary File Write via Symlink).
**Learning:** File(curr_dir, "index.html").writeText(...) 와 같은 방식을 사용할 때 대상 경로가 심볼릭 링크라면, 링크 자체가 덮어씌워지지 않고 링크가 가리키는 원본 파일 내용이 수정된다는 점을 배움. 악의적인 로컬 사용자가 이를 이용해 중요 시스템 파일을 파괴하거나 권한 상승을 시도할 위험이 존재함.
**Prevention:** 파일을 생성/쓰기 전에 `java.nio.file.Files.deleteIfExists` 등을 호출하여 대상 파일이나 심볼릭 링크를 명시적으로 삭제한 뒤, 안전하게 새 파일을 생성하는 방식으로 우회/방지할 수 있음.
