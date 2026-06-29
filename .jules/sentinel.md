## 2024-06-21 - [html4tree] Unsanitized Filenames in Auto-Generated HTML
**Vulnerability:** XSS via Malicious File/Directory Names
**Learning:** Tools that auto-generate static HTML pages from local file systems often overlook input sanitization, implicitly trusting local file paths. If these generated pages are hosted or shared, an attacker can create files with names like `<script>alert(1)</script>` to execute arbitrary JavaScript in the context of the user viewing the generated index.
**Prevention:** Always HTML-encode variable data injected into HTML templates, and URL-encode data used in `href` attributes, regardless of the data's origin (even if it's "just" the local file system). Additionally, ensure HTML attributes like `href` are properly quoted to prevent attribute breakout.

## 2024-06-29 - [html4tree] 심볼릭 링크를 통한 임의 파일 덮어쓰기 (Arbitrary File Write)
**Vulnerability:** 파일 시스템을 순회하며 `index.html`을 생성할 때, 대상 위치에 이미 악의적인 심볼릭 링크(symlink)가 존재할 경우 대상 파일을 무단으로 덮어쓰는 취약점 발생.
**Learning:** 파일 작성을 수행할 때 OS의 심볼릭 링크를 별도 확인 없이 따라가게 되면, 의도치 않은 시스템 주요 파일이나 다른 사용자의 파일을 변조할 수 있는 권한 상승 또는 데이터 파괴(Arbitrary File Write)가 발생할 수 있음을 확인.
**Prevention:** 파일을 쓰거나 덮어쓰기 전에 항상 `java.nio.file.Files.isSymbolicLink()`와 같은 메서드를 사용해 대상 파일이 심볼릭 링크인지 검사하고, 링크인 경우 이를 삭제한 후 안전하게 새 파일을 생성하도록 방어 로직을 구성해야 함.
