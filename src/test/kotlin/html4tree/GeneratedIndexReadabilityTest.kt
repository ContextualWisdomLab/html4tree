package html4tree

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.math.pow
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Product-level regressions for the CSS and markup emitted into a generated
 * directory index.
 *
 * These tests inspect the real `index.html` output rather than an independent
 * stylesheet fixture, so a template or CSP-byte regression cannot be hidden by
 * a test-only copy of the CSS.
 */
class GeneratedIndexReadabilityTest {
    private lateinit var temporaryDirectory: File

    @Before
    fun createTemporaryDirectory() {
        temporaryDirectory = Files.createTempDirectory("html4tree-readability-").toFile()
    }

    @After
    fun removeTemporaryDirectory() {
        temporaryDirectory.deleteRecursively()
    }

    @Test
    fun generatedRowsPreserveFirstMiddleAndLastOrder() {
        val firstFile = File(temporaryDirectory, "alpha.txt").apply { writeText("alpha") }
        val middleFile = File(temporaryDirectory, "middle.txt").apply { writeText("middle") }
        val lastFile = File(temporaryDirectory, "zulu.txt").apply { writeText("zulu") }

        process_dir(
            temporaryDirectory,
            setOf("index.html"),
            arrayOf(lastFile, firstFile, middleFile)
        )

        val generatedHtml = generatedHtml()
        val parentIndex = generatedHtml.indexOf("<span dir=\"auto\" aria-hidden=\"true\">..</span>")
        val firstIndex = generatedHtml.indexOf("alpha.txt")
        val middleIndex = generatedHtml.indexOf("middle.txt")
        val lastIndex = generatedHtml.indexOf("zulu.txt")

        assertTrue(parentIndex >= 0)
        assertTrue(parentIndex < firstIndex)
        assertTrue(firstIndex < middleIndex)
        assertTrue(middleIndex < lastIndex)
        assertFalse(generatedHtml.contains("이 디렉토리는 비어 있습니다."))
    }

    @Test
    fun emptyDirectoryRetainsOneSemanticStatusRow() {
        process_dir(temporaryDirectory, setOf("index.html"), emptyArray())

        val generatedHtml = generatedHtml()
        val expectedEmptyRow =
            """<li><div class="empty-dir" role="status"><span class="icon" aria-hidden="true">&#128194;</span> <span dir="auto">이 디렉토리는 비어 있습니다.</span></div></li>"""

        assertTrue(generatedHtml.contains(expectedEmptyRow))
        assertTrue(generatedHtml.indexOf(expectedEmptyRow) == generatedHtml.lastIndexOf(expectedEmptyRow))
    }

    @Test
    fun stylesheetSeparatesAdjacentRowsWithoutTrailingBorderRule() {
        process_dir(temporaryDirectory, setOf("index.html"), emptyArray())

        val style = emittedStyle()
        assertTrue(
            style.contains(
                """
                li + li {
                  border-top: 1px solid #d0d7de;
                }
                """.trimIndent()
            )
        )
        assertFalse(style.contains("li:last-child"))
    }

    @Test
    fun linkRowsRetainMobileTouchTargetPadding() {
        val linkedFile = File(temporaryDirectory, "touch-target.txt").apply { writeText("target") }
        process_dir(temporaryDirectory, setOf("index.html"), arrayOf(linkedFile))

        assertTrue(generatedHtml().contains("<a class=\"dir-link\""))
        val style = emittedStyle()
        assertTrue(
            style.contains(
                """
                a {
                  padding: 0.75rem 0.5rem;
                  text-decoration: none;
                """.trimIndent()
            )
        )
    }

    @Test
    fun emptyStateUsesExplicitLightAndDarkForegroundColors() {
        process_dir(temporaryDirectory, setOf("index.html"), emptyArray())

        val style = emittedStyle()
        val baseRule =
            """
            .empty-dir {
              display: flex;
              align-items: flex-start;
              gap: 0.5rem;
              padding: 0.75rem 0.5rem;
              color: #656d76;
              font-style: italic;
            }
            """.trimIndent()
        val darkModeMarker = "@media (prefers-color-scheme: dark)"
        val darkRule = "  .empty-dir {\n    color: #8b949e;\n  }"

        val baseRuleIndex = style.indexOf(baseRule)
        val darkModeIndex = style.indexOf(darkModeMarker)
        val darkRuleIndex = style.indexOf(darkRule, startIndex = darkModeIndex.coerceAtLeast(0))

        assertTrue(baseRuleIndex >= 0)
        assertFalse(style.contains("opacity:"))
        assertTrue(darkModeIndex > baseRuleIndex)
        assertTrue(darkRuleIndex > darkModeIndex)
    }

    @Test
    fun hoverAndKeyboardFocusUnderlineOnlyLinkText() {
        process_dir(temporaryDirectory, setOf("index.html"), emptyArray())

        val style = emittedStyle()
        val completeTargetRule = Regex("""a:hover, a:focus-visible \{([\s\S]*?)\}""")
            .find(style)
            ?.groupValues
            ?.get(1)
        assertNotNull(completeTargetRule)
        assertFalse(completeTargetRule.contains("text-decoration"))
        assertTrue(completeTargetRule.contains("outline: 2px solid #0969da;"))
        assertTrue(
            style.contains(
                """
                a:hover span:last-child, a:focus-visible span:last-child {
                  text-decoration: underline;
                }
                """.trimIndent()
            )
        )
        assertTrue(style.contains("@media (prefers-reduced-motion: reduce)"))
    }

    @Test
    fun authoredColorsMeetDocumentedContrastThresholds() {
        assertTrue(contrastRatio("#656d76", "#ffffff") >= 4.5)
        assertTrue(contrastRatio("#8b949e", "#0d1117") >= 4.5)
        assertTrue(contrastRatio("#0969da", "#f6f8fa") >= 3.0)
        assertTrue(contrastRatio("#58a6ff", "#161b22") >= 3.0)
    }

    private fun generatedHtml(): String =
        File(temporaryDirectory, "index.html").readText(Charsets.UTF_8)

    private fun emittedStyle(): String {
        val style = Regex("""<style>([\s\S]*?)</style>""")
            .find(generatedHtml())
            ?.groupValues
            ?.get(1)
        return requireNotNull(style) { "Generated HTML must contain one inline style block" }
    }

    private fun contrastRatio(foreground: String, background: String): Double {
        val foregroundLuminance = relativeLuminance(foreground)
        val backgroundLuminance = relativeLuminance(background)
        val lighter = maxOf(foregroundLuminance, backgroundLuminance)
        val darker = minOf(foregroundLuminance, backgroundLuminance)
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun relativeLuminance(hexColor: String): Double {
        val channels = hexColor.removePrefix("#")
            .chunked(2)
            .map { it.toInt(16) / 255.0 }
            .map { channel ->
                if (channel <= 0.04045) {
                    channel / 12.92
                } else {
                    ((channel + 0.055) / 1.055).pow(2.4)
                }
            }
        return (0.2126 * channels[0]) + (0.7152 * channels[1]) + (0.0722 * channels[2])
    }
}
