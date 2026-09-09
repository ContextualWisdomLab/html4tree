package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertTrue

class BidiIsolationTest {
    private fun containsIsolatedText(html: String, prefix: String, text: String, suffix: String): Boolean {
        return html.contains("$prefix\u2068$text\u2069$suffix") ||
            html.contains("$prefix&#x2068;$text&#x2069;$suffix")
    }

    @Test
    fun ordinaryRtlNamesAreIsolatedFromFixedUiText() {
        val root = Files.createTempDirectory("html4tree-bidi-").toFile()
        val directory = File(root, "ملفات")
        assertTrue(directory.mkdir())
        val file = File(directory, "تقرير.txt")
        assertTrue(file.createNewFile())

        try {
            process_dir(directory, excludeSet = emptySet(), dirFiles = arrayOf(file))
            val html = File(directory, "index.html").readText()
            val escapedDirectory = directory.name.escapeHtml()
            val escapedFile = file.name.escapeHtml()

            // Unknown-direction visible names need native HTML direction isolation even
            // when they contain no suspicious formatting controls.
            assertTrue(html.contains("<h1 dir=\"auto\">$escapedDirectory</h1>"))
            assertTrue(html.contains("<span dir=\"auto\">$escapedFile</span>"))

            // <title> and title="..." cannot contain isolating markup. Isolate only
            // the dynamic name so adjacent fixed Korean UI text keeps its ordering.
            assertTrue(
                containsIsolatedText(
                    html,
                    "<title>",
                    escapedDirectory,
                    " - 디렉토리 목록</title>",
                )
            )
            assertTrue(
                containsIsolatedText(
                    html,
                    "title=\"",
                    escapedFile,
                    " 파일\"",
                )
            )
        } finally {
            root.deleteRecursively()
        }
    }
}
