package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertTrue

class BidiTextDirectionTest {
    @Test
    fun processDirMarksRtlUserTextForAutomaticDirection() {
        val root = Files.createTempDirectory("html4tree-bidi-").toFile()
        val rtlDirectory = File(root, "ملف")
        val rtlFile = File(rtlDirectory, "מסמך.txt")

        try {
            assertTrue(rtlDirectory.mkdir())
            rtlFile.writeText("fixture")

            process_dir(rtlDirectory, emptySet(), arrayOf(rtlFile))

            val html = File(rtlDirectory, "index.html").readText()
            assertTrue(html.contains("<title dir=\"auto\">ملف - 디렉토리 목록</title>"))
            assertTrue(html.contains("<h1 dir=\"auto\">ملف</h1>"))
            assertTrue(html.contains("<span dir=\"auto\">מסמך.txt</span>"))
        } finally {
            root.deleteRecursively()
        }
    }
}
