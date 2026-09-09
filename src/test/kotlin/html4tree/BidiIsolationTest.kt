package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertTrue

class BidiIsolationTest {
    @Test
    fun testBidiIsolationCoversVisibleLabelsAndNonMarkupTextContexts() {
        val root = Files.createTempDirectory("html4tree-bidi-").toFile()
        val directory = File(root, "safe\u202Egpj")
        assertTrue(directory.mkdir())
        val file = File(directory, "report\u202Egpj.exe")
        assertTrue(file.createNewFile())

        try {
            process_dir(directory, excludeSet = emptySet(), dirFiles = arrayOf(file))
            val html = File(directory, "index.html").readText()
            val escapedDirectory = directory.name.escapeHtml()
            val escapedFile = file.name.escapeHtml()

            // Markup contexts can use dir=auto, which HTML renders as a bidi isolate.
            assertTrue(html.contains("<h1 dir=\"auto\">$escapedDirectory</h1>"))
            assertTrue(html.contains("<span dir=\"auto\">$escapedFile</span>"))

            // <title> and title="..." cannot contain isolating markup. Keep the
            // untrusted name in an FSI/PDI pair so it cannot reorder fixed UI text.
            assertTrue(
                html.contains(
                    "<title>&#x2068;$escapedDirectory&#x2069; - 디렉토리 목록</title>"
                )
            )
            assertTrue(
                html.contains("title=\"&#x2068;$escapedFile&#x2069; 파일\"")
            )
        } finally {
            root.deleteRecursively()
        }
    }
}
