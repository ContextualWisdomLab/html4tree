package html4tree

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Buyer-facing regressions for Trojan Source filenames.
 *
 * A purchaser must not see `invoice.txt` when the real name is
 * `invoice.txt` + RLO + `exe`. The generated page keeps the real
 * `href` so the file still opens, and shows U+FFFD where a
 * bidirectional format control was removed.
 */
class TrojanSourceSecurityTest {
    private lateinit var temporaryDirectory: File

    @Before
    fun createTemporaryDirectory() {
        temporaryDirectory = Files.createTempDirectory("html4tree-trojan-").toFile()
    }

    @After
    fun removeTemporaryDirectory() {
        temporaryDirectory.deleteRecursively()
    }

    @Test
    fun isBidiControlCoversStatefulAndMarkControlsOnly() {
        assertTrue(is_bidi_control('\u061C'))
        assertTrue(is_bidi_control('\u200E'))
        assertTrue(is_bidi_control('\u200F'))
        assertTrue(is_bidi_control('\u202A'))
        assertTrue(is_bidi_control('\u202B'))
        assertTrue(is_bidi_control('\u202C'))
        assertTrue(is_bidi_control('\u202D'))
        assertTrue(is_bidi_control('\u202E'))
        assertTrue(is_bidi_control('\u2066'))
        assertTrue(is_bidi_control('\u2067'))
        assertTrue(is_bidi_control('\u2068'))
        assertTrue(is_bidi_control('\u2069'))
        assertFalse(is_bidi_control('A'))
        assertFalse(is_bidi_control('\u061B'))
        assertFalse(is_bidi_control('\u200D'))
        assertFalse(is_bidi_control('\u2029'))
        assertFalse(is_bidi_control('\u2065'))
        assertFalse(is_bidi_control('\u206A'))
    }

    @Test
    fun neutralizeReplacesControlsAndLeavesOrdinaryTextUntouched() {
        assertEquals("", neutralize_bidi_controls(""))
        assertEquals("invoice.txt", neutralize_bidi_controls("invoice.txt"))
        assertEquals("invoice.txt\uFFFDexe", neutralize_bidi_controls("invoice.txt\u202Eexe"))
        assertEquals("\uFFFD\uFFFD", neutralize_bidi_controls("\u2066\u2069"))
        assertEquals("a\uFFFDb\uFFFDc", neutralize_bidi_controls("a\u200Eb\u200Fc"))
        assertEquals("keep", neutralize_bidi_controls("keep"))
    }

    @Test
    fun escapeHtmlKeepsFirstStrongAndPopDirectionalIsolates() {
        val isolated = isolate_bidi_plain_text("invoice.txt")
        assertEquals(isolated, isolated.escapeHtml())
        assertTrue(isolated.startsWith("\u2068"))
        assertTrue(isolated.endsWith("\u2069"))
    }

    @Test
    fun generatedListingNeutralizesRloSpoofAndKeepsRealHref() {
        val spoofedName = "invoice.txt\u202Eexe"
        val spoofedFile = File(temporaryDirectory, spoofedName).apply { writeText("payload") }

        process_dir(temporaryDirectory, setOf("index.html"), arrayOf(spoofedFile))

        val generatedHtml = File(temporaryDirectory, "index.html").readText(Charsets.UTF_8)
        val encodedHref = "./${spoofedName.urlEncodePath()}"
        val displayName = "invoice.txt\uFFFDexe"

        assertTrue(generatedHtml.contains("<span class=\"entry-name\" dir=\"auto\">$displayName</span>"))
        assertTrue(generatedHtml.contains("title=\"${isolate_bidi_plain_text(displayName)} 파일\""))
        assertTrue(generatedHtml.contains("이름에 방향 제어 문자가 있습니다"))
        assertTrue(generatedHtml.contains("href=\"$encodedHref\""))
        assertFalse(generatedHtml.contains("invoice.txt\u202Eexe</span>"))
        assertFalse(generatedHtml.contains("title=\"${isolate_bidi_plain_text(spoofedName)}"))
    }
}
