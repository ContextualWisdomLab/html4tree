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
 * Buyer-facing regressions for mixed-direction directory names.
 *
 * A purchaser opening an Arabic or Hebrew tree must see the real filename,
 * a stable Korean type label, and a tooltip that cannot be reordered by
 * the Unicode Bidirectional Algorithm.
 */
class BidiIsolationTest {
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
    fun isolatePlainTextWrapsFirstStrongAndPopDirectionalIsolates() {
        assertEquals("\u2068\u2069", isolate_bidi_plain_text(""))
        assertEquals("\u2068report.pdf\u2069", isolate_bidi_plain_text("report.pdf"))
        assertEquals("\u2068تقرير.pdf\u2069", isolate_bidi_plain_text("تقرير.pdf"))
    }

    @Test
    fun arabicAndHebrewFilenamesStayIsolatedFromKoreanTypeLabels() {
        val arabicReport = File(temporaryDirectory, "تقرير.pdf").apply { writeText("report") }
        val hebrewInvoice = File(temporaryDirectory, "חשבונית.txt").apply { writeText("invoice") }
        val mixedMinutes = File(temporaryDirectory, "회의-محضر-2026.txt").apply { writeText("minutes") }

        process_dir(
            temporaryDirectory,
            setOf("index.html"),
            arrayOf(arabicReport, hebrewInvoice, mixedMinutes)
        )

        val generatedHtml = File(temporaryDirectory, "index.html").readText(Charsets.UTF_8)

        assertTrue(generatedHtml.contains("<h1 dir=\"auto\">"))
        assertTrue(generatedHtml.contains("unicode-bidi: isolate;"))

        listOf(
            Triple("تقرير.pdf", "파일", "./%D8%AA%D9%82%D8%B1%D9%8A%D8%B1.pdf"),
            Triple("חשבונית.txt", "파일", "./%D7%97%D7%A9%D7%91%D7%95%D7%A0%D7%99%D7%AA.txt"),
            Triple("회의-محضر-2026.txt", "파일", "./%ED%9A%8C%EC%9D%98-%D9%85%D8%AD%D8%B6%D8%B1-2026.txt")
        ).forEach { (fileName, typeLabel, encodedHref) ->
            val isolatedName = """<span class="entry-name" dir="auto">${fileName.escapeHtml()}</span>"""
            val hiddenType = """<span class="visually-hidden">$typeLabel</span>"""
            val isolatedTitle = """title="${isolate_bidi_plain_text(fileName)} $typeLabel""""

            assertTrue(generatedHtml.contains(isolatedName), "visible name must be an isolated dir=auto span: $fileName")
            assertTrue(generatedHtml.contains(hiddenType), "Korean type label must stay outside the isolate: $fileName")
            assertTrue(generatedHtml.contains(isolatedTitle), "title must wrap the filename in FSI/PDI: $fileName")
            assertTrue(generatedHtml.contains("href=\"$encodedHref\""), "href must percent-encode the real UTF-8 name: $fileName")
            assertTrue(
                generatedHtml.indexOf(isolatedName) < generatedHtml.indexOf(hiddenType, generatedHtml.indexOf(isolatedName)),
                "type label must follow the isolated filename: $fileName"
            )
        }

        assertFalse(generatedHtml.contains("aria-label=\"تقرير"), "entry names must not be stuffed into aria-label")
        assertTrue(generatedHtml.contains("<nav aria-label=\"디렉토리 목록\">"))
    }

    @Test
    fun rtlDirectoryHeadingKeepsParentNavigationReadable() {
        val arabicDirectory = File(temporaryDirectory, "الفواتير")
        arabicDirectory.mkdir()
        File(arabicDirectory, "note.txt").writeText("note")

        process_dir(arabicDirectory, setOf("index.html"), arrayOf(File(arabicDirectory, "note.txt")))

        val generatedHtml = File(arabicDirectory, "index.html").readText(Charsets.UTF_8)
        assertTrue(generatedHtml.contains("<h1 dir=\"auto\">الفواتير</h1>"))
        assertTrue(generatedHtml.contains("<span class=\"entry-name\" aria-hidden=\"true\">..</span>"))
        assertTrue(generatedHtml.contains("<span class=\"visually-hidden\">상위 디렉토리로 이동</span>"))
        assertTrue(generatedHtml.contains("<span class=\"entry-name\" dir=\"auto\">note.txt</span>"))
    }
}
