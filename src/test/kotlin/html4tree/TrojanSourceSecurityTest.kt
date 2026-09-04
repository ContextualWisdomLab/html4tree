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
 * Buyer-facing regressions for bidirectional-control filenames.
 *
 * Display text must make control characters visible without changing the real
 * filesystem name used by the link target.
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
    fun bidiControlClassificationCoversFormattingControls() {
        val controls = listOf(
            '\u061C', '\u200E', '\u200F',
            '\u202A', '\u202B', '\u202C', '\u202D', '\u202E',
            '\u2066', '\u2067', '\u2068', '\u2069'
        )
        controls.forEach { assertTrue(is_bidi_control(it)) }
        listOf('A', '\u061B', '\u200D', '\u2029', '\u2065', '\u206A').forEach {
            assertFalse(is_bidi_control(it))
        }
    }

    @Test
    fun neutralizationReplacesControlsWithoutChangingOrdinaryText() {
        assertEquals("", neutralize_bidi_controls(""))
        assertEquals("invoice.txt", neutralize_bidi_controls("invoice.txt"))
        assertEquals("invoice.txt\uFFFDexe", neutralize_bidi_controls("invoice.txt\u202Eexe"))
        assertEquals("\uFFFD\uFFFD", neutralize_bidi_controls("\u2066\u2069"))
        assertEquals("a\uFFFDb\uFFFDc", neutralize_bidi_controls("a\u200Eb\u200Fc"))
    }

    @Test
    fun htmlEscapingPreservesRendererOwnedIsolationMarks() {
        val isolated = isolate_bidi_plain_text("invoice.txt")
        assertEquals(isolated, isolated.escapeHtml())
        assertTrue(isolated.startsWith("\u2068"))
        assertTrue(isolated.endsWith("\u2069"))
    }

    @Test
    fun generatedListingNeutralizesSpoofButKeepsExactHref() {
        val spoofedName = "invoice.txt\u202Eexe"
        val spoofedFile = File(temporaryDirectory, spoofedName).apply { writeText("payload") }

        process_dir(temporaryDirectory, setOf("index.html"), arrayOf(spoofedFile))

        val generatedHtml = File(temporaryDirectory, "index.html").readText(Charsets.UTF_8)
        val encodedHref = "./${spoofedName.urlEncodePath()}"
        val displayName = "invoice.txt\uFFFDexe"

        assertTrue(generatedHtml.contains("<span dir=\"auto\">$displayName</span>"))
        assertTrue(generatedHtml.contains("title=\"${isolate_bidi_plain_text(displayName)} 파일\""))
        assertTrue(generatedHtml.contains("이름에 방향 제어 문자가 있습니다. 열기 전에 링크 대상 파일 이름을 확인하세요."))
        assertTrue(generatedHtml.contains("href=\"$encodedHref\""))
        assertFalse(generatedHtml.contains("$spoofedName</span>"))
        assertFalse(generatedHtml.contains("title=\"${isolate_bidi_plain_text(spoofedName)}"))
    }

    @Test
    fun directoryHeadingAndTabTitleNeutralizeControls() {
        val spoofedDirectory = File(temporaryDirectory, "invoices\u202E")
        spoofedDirectory.mkdir()
        val note = File(spoofedDirectory, "note.txt").apply { writeText("note") }

        process_dir(spoofedDirectory, setOf("index.html"), arrayOf(note))

        val generatedHtml = File(spoofedDirectory, "index.html").readText(Charsets.UTF_8)
        val displayName = "invoices\uFFFD"
        assertTrue(generatedHtml.contains("<h1 dir=\"auto\">$displayName</h1>"))
        assertTrue(generatedHtml.contains("<title>${isolate_bidi_plain_text(displayName)} - 디렉토리 목록</title>"))
        assertFalse(generatedHtml.contains("<h1 dir=\"auto\">invoices\u202E</h1>"))
    }
}
