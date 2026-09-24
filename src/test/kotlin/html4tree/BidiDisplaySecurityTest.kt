package html4tree

import java.io.File
import java.nio.file.Files
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BidiDisplaySecurityTest {
    private val bidiControls = linkedMapOf(
        '\u061C' to "\\u061C",
        '\u200E' to "\\u200E",
        '\u200F' to "\\u200F",
        '\u202A' to "\\u202A",
        '\u202B' to "\\u202B",
        '\u202C' to "\\u202C",
        '\u202D' to "\\u202D",
        '\u202E' to "\\u202E",
        '\u2066' to "\\u2066",
        '\u2067' to "\\u2067",
        '\u2068' to "\\u2068",
        '\u2069' to "\\u2069"
    )

    @Test
    fun escapeHtmlNeutralizesEveryBidiControl() {
        for ((control, escapedControl) in bidiControls) {
            val input = "left${control}right"
            val escaped = input.escapeHtml()
            assertEquals("left${escapedControl}right", escaped)
            assertFalse(escaped.contains(control))
        }
    }

    @Test
    fun escapeHtmlPreservesNaturalRtlAndLtrText() {
        assertEquals("مرحبا-파일&lt;&amp;&quot;", "مرحبا-파일<&\"".escapeHtml())
    }

    @Test
    fun generatedListingNeutralizesControlsAndUsesHtmlIsolation() {
        val root = Files.createTempDirectory("html4tree-bidi").toFile()
        try {
            val names = bidiControls.keys.mapIndexed { index, control ->
                "safe${control}name${index}.txt"
            }
            val files = names.map { name -> File(root, name) }.toTypedArray()

            process_dir(root, emptySet<String>(), files)

            val html = File(root, "index.html").readText()
            for ((index, entry) in bidiControls.entries.withIndex()) {
                val control = entry.key
                val name = names[index]
                assertFalse(html.contains(control))
                assertTrue(html.contains("href=\"./${name.urlEncodePath()}\""))
                assertTrue(html.contains("<bdi dir=\"auto\">${name.escapeHtml()}</bdi>"))
                assertTrue(html.contains("title=\"${name.escapeHtml()} 파일\""))
            }
            assertFalse(html.contains("<span>&#x2068;"))
            assertFalse(html.contains("&#x2069;</span>"))
        } finally {
            root.deleteRecursively()
        }
    }
}
