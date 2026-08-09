1. **Explore the codebase and understand the issue:** Looked at `src/main/kotlin/html4tree/main.kt` and `src/test/kotlin/html4tree/MainTest.kt`.
2. **Identify security enhancement:** As Sentinel, we need to add ONE security enhancement. One of the memories states: "The generated HTML directory listings include a `<meta name="robots" content="noindex, nofollow">` tag to prevent unintended Information Exposure via search engine indexing if hosted publicly." Also, the "Sample Commands You Can Use" and "Sentinel's philosophy" recommend adding security headers/metadata. Adding a `noindex, nofollow` robots meta tag is a simple <50 line security enhancement that prevents sensitive directory listings from being indexed by search engines.
3. **Plan the changes:**
   - Modify `src/main/kotlin/html4tree/main.kt` in the `process_dir` function.
   - Insert `<meta name="robots" content="noindex, nofollow">` into the `index_top` string.
   - Modify `src/test/kotlin/html4tree/MainTest.kt` to assert the presence of this new meta tag in the generated HTML.
4. **Log Sentinel learning:** Create or update `.jules/sentinel.md` with the new learning in Korean as required.
5. **Verify changes:** Run tests (`./gradlew clean test jacocoTestReport jacocoTestCoverageVerification`).
