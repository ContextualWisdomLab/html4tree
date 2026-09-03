package html4tree

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.FileTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Buyer-facing regressions for Apache-style size and last-modified
 * columns. A purchaser comparing two similarly named files must see
 * the real byte size and the filesystem mtime, not a guessed value.
 */
class ListingMetadataTest {
    private lateinit var temporaryDirectory: File

    @Before
    fun createTemporaryDirectory() {
        temporaryDirectory = Files.createTempDirectory("html4tree-meta-").toFile()
    }

    @After
    fun removeTemporaryDirectory() {
        temporaryDirectory.deleteRecursively()
    }

    @Test
    fun formatByteSizeUsesIecBinaryPrefixes() {
        assertEquals("0 B", format_byte_size(-3L))
        assertEquals("0 B", format_byte_size(0L))
        assertEquals("1 B", format_byte_size(1L))
        assertEquals("1023 B", format_byte_size(1023L))
        assertEquals("1.0 KiB", format_byte_size(1024L))
        assertEquals("1.5 KiB", format_byte_size(1536L))
        assertEquals("1.0 MiB", format_byte_size(1024L * 1024L))
        assertEquals("1.0 GiB", format_byte_size(1024L * 1024L * 1024L))
        assertEquals("2.0 GiB", format_byte_size(2L * 1024L * 1024L * 1024L))
        assertEquals("1.0 KiB", format_scaled_size(1024L, 1024L, "KiB"))
        assertEquals("0 B", format_scaled_size(-1L, 1024L, "KiB"))
        assertEquals("0 B", format_scaled_size(2048L, 0L, "KiB"))
        assertEquals("0 B", format_scaled_size(2048L, -1024L, "KiB"))
        assertEquals("${Long.MAX_VALUE / (1024L * 1024L * 1024L)}.0 GiB", format_scaled_size(Long.MAX_VALUE, 1024L * 1024L * 1024L, "GiB"))
    }

    @Test
    fun formatUtcMinuteAndIsoInstantUseKnownEpoch() {
        assertEquals("1970-01-01T00:00:00Z", format_iso_instant(0L))
        assertEquals("1970-01-01 00:00 UTC", format_utc_minute(0L))
        assertEquals("2024-08-18T16:53:20Z", format_iso_instant(1_724_000_000_000L))
        assertEquals("2024-08-18 16:53 UTC", format_utc_minute(1_724_000_000_000L))
        assertEquals("\u2014", directory_size_label())
    }

    @Test
    fun generatedFileRowShowsExactByteSizeAndFilesystemMtime() {
        val minutes = File(temporaryDirectory, "minutes.txt").apply { writeText("hello world") }
        val expectedSize = minutes.length()
        val expectedMillis = Files.getLastModifiedTime(minutes.toPath()).toMillis()
        Files.setLastModifiedTime(minutes.toPath(), FileTime.fromMillis(expectedMillis))

        process_dir(temporaryDirectory, setOf("index.html"), arrayOf(minutes))

        val generatedHtml = File(temporaryDirectory, "index.html").readText(Charsets.UTF_8)
        val expectedSizeLabel = format_byte_size(expectedSize)
        val expectedIso = format_iso_instant(expectedMillis)
        val expectedDisplay = format_utc_minute(expectedMillis)

        assertEquals(11L, expectedSize)
        assertEquals("11 B", expectedSizeLabel)
        assertTrue(generatedHtml.contains("<span class=\"entry-size\">$expectedSizeLabel</span>"))
        assertTrue(
            generatedHtml.contains(
                """<time class="entry-mtime" datetime="$expectedIso" dir="ltr">$expectedDisplay</time>"""
            )
        )
        assertTrue(generatedHtml.contains("<span class=\"entry-name\" dir=\"auto\">minutes.txt</span>"))
        assertTrue(generatedHtml.contains("--listing-meta: #656d76;"))
        assertTrue(generatedHtml.contains("--listing-dark-meta: #8b949e;"))
    }

    @Test
    fun generatedDirectoryRowUsesEmDashSizeAndKeepsMtime() {
        val invoices = File(temporaryDirectory, "invoices")
        invoices.mkdir()
        File(invoices, "kept.txt").writeText("kept")
        val expectedMillis = Files.getLastModifiedTime(invoices.toPath()).toMillis()

        process_dir(temporaryDirectory, setOf("index.html"), arrayOf(invoices))

        val generatedHtml = File(temporaryDirectory, "index.html").readText(Charsets.UTF_8)
        assertTrue(generatedHtml.contains("<span class=\"entry-size\">\u2014</span>"))
        assertTrue(generatedHtml.contains("datetime=\"${format_iso_instant(expectedMillis)}\""))
        assertTrue(generatedHtml.contains("<span class=\"visually-hidden\">디렉토리</span>"))
    }

    @Test
    fun vanishedEntryOmitsMetadataAndStillRendersTheName() {
        val ghost = File(temporaryDirectory, "ghost.txt")

        process_dir(temporaryDirectory, setOf("index.html"), arrayOf(ghost))

        val generatedHtml = File(temporaryDirectory, "index.html").readText(Charsets.UTF_8)
        assertTrue(generatedHtml.contains("<span class=\"entry-name\" dir=\"auto\">ghost.txt</span>"))
        assertFalse(generatedHtml.contains("ghost.txt</span> <span class=\"visually-hidden\">파일</span> <span class=\"entry-meta\""))
        assertFalse(generatedHtml.contains("이름에 방향 제어 문자가 있습니다"))
    }
}
