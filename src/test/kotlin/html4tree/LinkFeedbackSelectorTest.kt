package html4tree

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Regression coverage for the generated link-label styling contract. */
class LinkFeedbackSelectorTest {
    private lateinit var temporaryDirectory: File

    @Before
    fun createTemporaryDirectory() {
        temporaryDirectory = Files.createTempDirectory("html4tree-link-feedback-").toFile()
    }

    @After
    fun removeTemporaryDirectory() {
        temporaryDirectory.deleteRecursively()
    }

    @Test
    fun hoverAndKeyboardFocusTargetTheNamedVisibleLabelRatherThanChildPosition() {
        val linkedFile = File(temporaryDirectory, "report.txt").apply { writeText("report") }
        process_dir(temporaryDirectory, setOf("index.html"), arrayOf(linkedFile))

        val generatedHtml = File(temporaryDirectory, "index.html").readText(Charsets.UTF_8)
        val style = requireNotNull(
            Regex("""<style>([\s\S]*?)</style>""")
                .find(generatedHtml)
                ?.groupValues
                ?.get(1)
        )

        assertTrue(
            generatedHtml.contains(
                """<span class="entry-label" aria-hidden="true">..</span>"""
            )
        )
        assertTrue(generatedHtml.contains("""<span class="entry-label">report.txt</span>"""))
        assertTrue(
            style.contains(
                """
                a:hover .entry-label, a:focus-visible .entry-label {
                  text-decoration: underline;
                }
                """.trimIndent()
            )
        )
        assertFalse(style.contains("nth-last-child"))
        assertFalse(style.contains("last-child"))
    }
}
