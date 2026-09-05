package html4tree

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NavigationLandmarkAccessibilityTest {
    private lateinit var tempDir: File

    @Before
    fun setup() {
        tempDir = Files.createTempDirectory("html4tree-nav-landmark-").toFile()
        File(tempDir, "report.txt").writeText("report")
    }

    @After
    fun teardown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun generatedNavigationUsesOneHeadingAsItsAccessibleNameSource() {
        process_dir(tempDir)

        val html = File(tempDir, "index.html").readText()
        val headingId = "nav-heading"

        assertEquals(1, Regex("id=\\\"$headingId\\\"").findAll(html).count())
        assertEquals(1, Regex("aria-labelledby=\\\"$headingId\\\"").findAll(html).count())
        assertTrue(
            html.contains(
                "<h2 id=\"$headingId\" class=\"visually-hidden\">디렉토리 목록</h2>"
            )
        )
        assertFalse(html.contains("<nav aria-label="))
    }
}
