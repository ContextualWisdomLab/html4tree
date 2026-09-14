package html4tree

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Product-level regressions for bidirectional-control spoofing in generated
 * directory indexes. The raw path remains usable in href while security-
 * sensitive human-readable labels must expose invisible direction controls.
 */
class BidiSpoofingSecurityTest {
    private lateinit var temporaryRoot: File

    @Before
    fun createTemporaryRoot() {
        temporaryRoot = Files.createTempDirectory("html4tree-bidi-").toFile()
    }

    @After
    fun removeTemporaryRoot() {
        temporaryRoot.deleteRecursively()
    }

    @Test
    fun fileNameExposesBidiOverrideWithoutChangingTargetPath() {
        val fileName = "invoice\u202Efdp.exe"
        val file = File(temporaryRoot, fileName).apply { writeText("payload") }

        process_dir(temporaryRoot, setOf("index.html"), arrayOf(file))

        val html = generatedHtml(temporaryRoot)
        val safeDisplayName = "invoice\\u202Efdp.exe"

        assertFalse(html.contains('\u202E'))
        assertTrue(html.contains("href=\"./invoice%E2%80%AEfdp.exe\""))
        assertTrue(html.contains("title=\"$safeDisplayName 파일\""))
        assertTrue(html.contains("<span dir=\"auto\">$safeDisplayName</span>"))
    }

    @Test
    fun directoryHeadingExposesBidiControlsAndUsesAutomaticDirectionIsolation() {
        val directoryName = "reports\u2067evil\u2069"
        val directory = File(temporaryRoot, directoryName).apply { mkdir() }

        process_dir(directory, setOf("index.html"), emptyArray())

        val html = generatedHtml(directory)
        val safeDisplayName = "reports\\u2067evil\\u2069"

        assertFalse(html.contains('\u2067'))
        assertFalse(html.contains('\u2069'))
        assertTrue(html.contains("<title>$safeDisplayName - 디렉토리 목록</title>"))
        assertTrue(html.contains("<h1 dir=\"auto\">$safeDisplayName</h1>"))
    }

    @Test
    fun ordinaryRtlFileNamesRemainReadableWithoutEscapingTheirLetters() {
        val fileName = "דוח.txt"
        val file = File(temporaryRoot, fileName).apply { writeText("payload") }

        process_dir(temporaryRoot, setOf("index.html"), arrayOf(file))

        val html = generatedHtml(temporaryRoot)
        assertTrue(html.contains("title=\"$fileName 파일\""))
        assertTrue(html.contains("<span dir=\"auto\">$fileName</span>"))
    }

    private fun generatedHtml(directory: File): String =
        File(directory, "index.html").readText(Charsets.UTF_8)
}
