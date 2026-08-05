package html4tree

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertTrue

/**
 * Regression coverage for the generated empty-directory status markup.
 *
 * The assertion deliberately matches the complete empty-state fragment so an
 * unrelated decorative icon elsewhere in the page cannot make this test pass.
 */
class EmptyDirectoryMarkupTest {
    private lateinit var temporaryDirectory: File

    @Before
    fun setUpTemporaryDirectory() {
        temporaryDirectory = Files.createTempDirectory("html4tree-empty-state-").toFile()
    }

    @After
    fun removeTemporaryDirectory() {
        temporaryDirectory.deleteRecursively()
    }

    @Test
    fun emptyDirectoryIconIsHiddenInsideStatusMessage() {
        go(temporaryDirectory.absolutePath, -1)

        val generatedHtml = File(temporaryDirectory, "index.html").readText()
        val expectedEmptyState =
            """<div class="empty-dir" role="status"><span class="icon" aria-hidden="true">&#128194;</span> <span>이 디렉토리는 비어 있습니다.</span></div>"""

        assertTrue(
            generatedHtml.contains(expectedEmptyState),
            "The decorative folder icon must remain aria-hidden inside the empty-directory status element."
        )
    }
}
