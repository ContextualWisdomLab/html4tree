package html4tree

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Generated-output contract for externally supplied text with unknown direction. */
class BidirectionalTextDirectionTest {
    private lateinit var temporaryDirectory: File

    @Before
    fun createTemporaryDirectory() {
        temporaryDirectory = Files.createTempDirectory("html4tree-bidi-").toFile()
    }

    @After
    fun removeTemporaryDirectory() {
        temporaryDirectory.deleteRecursively()
    }

    @Test
    fun dynamicDirectoryAndFilenameUseAutoWithoutReclassifyingFixedCopy() {
        val generatedDirectory = File(temporaryDirectory, "دليل-Guide").apply { mkdir() }
        val linkedFile = File(generatedDirectory, "تقرير-report.txt").apply { writeText("report") }

        process_dir(generatedDirectory, setOf("index.html"), arrayOf(linkedFile))

        val html = File(generatedDirectory, "index.html").readText(Charsets.UTF_8)
        assertTrue(html.contains("<h1 dir=\"auto\">دليل-Guide</h1>"))
        assertTrue(html.contains("<span class=\"entry-name\" dir=\"auto\">تقرير-report.txt</span>"))

        val parentRow = Regex("""<a class="dir-link" href="\./\.\."[\s\S]*?</a>""")
            .find(html)
            ?.value
        assertTrue(parentRow != null)
        assertTrue(parentRow!!.contains("<span class=\"entry-name\" aria-hidden=\"true\">..</span>"))
        assertFalse(parentRow.contains("dir=\"auto\""))

        val fileRow = Regex("""<a class="dir-link"[^>]*>[\s\S]*?تقرير-report\.txt[\s\S]*?</a>""")
            .find(html)
            ?.value
        assertTrue(fileRow != null)
        assertTrue(fileRow!!.contains("<span class=\"visually-hidden\">파일</span>"))
        assertFalse(fileRow.contains("<span class=\"visually-hidden\" dir=\"auto\">"))
    }
}
