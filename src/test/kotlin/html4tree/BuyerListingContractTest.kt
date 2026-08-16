package html4tree

import java.io.File
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.attribute.BasicFileAttributes
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * One `go()` tree a purchaser would actually publish: meeting notes,
 * an ignored temp file, a leaked secret, an Arabic name, and a nested
 * invoices folder. The generated pages must keep the notes, hide the
 * secret and temp file, isolate the Arabic name, show the real size
 * and UTC mtime, omit `..` on the crawl root, and keep `..` one level
 * down.
 */
class BuyerListingContractTest {
    private lateinit var publishedTree: File

    @Before
    fun createPublishedTree() {
        publishedTree = Files.createTempDirectory("html4tree-buyer-").toFile()
    }

    @After
    fun removePublishedTree() {
        publishedTree.deleteRecursively()
    }

    @Test
    fun sameNormalizedPathComparesAbsoluteNormalizedPaths() {
        val invoices = File(publishedTree, "invoices")
        invoices.mkdir()
        assertTrue(same_normalized_path(publishedTree, publishedTree))
        assertTrue(same_normalized_path(publishedTree, File(publishedTree.absolutePath)))
        assertFalse(same_normalized_path(publishedTree, invoices))
    }

    @Test
    fun processDirCanOmitTheParentRowWithoutChangingKeepHideRules() {
        val minutes = File(publishedTree, "minutes.txt").apply { writeText("hello world") }

        process_dir(
            publishedTree,
            setOf("index.html"),
            arrayOf(minutes),
            includeParentLink = false
        )

        val generatedHtml = File(publishedTree, "index.html").readText(Charsets.UTF_8)
        assertTrue(generatedHtml.contains("href=\"./minutes.txt\""))
        assertFalse(generatedHtml.contains("href=\"./..\""))
        assertFalse(generatedHtml.contains("상위 디렉토리로 이동"))
    }

    @Test
    fun goOnAPublishedTreeKeepsNotesHidesSecretsAndOmitsRootParent() {
        File(publishedTree, ".html4ignore").writeText("*.tmp\n")
        val minutes = File(publishedTree, "minutes.txt").apply { writeText("hello world") }
        File(publishedTree, "scratch.tmp").writeText("scratch")
        File(publishedTree, ".env").writeText("SECRET=1")
        val arabicReport = File(publishedTree, "تقرير.pdf").apply { writeText("report") }
        val invoices = File(publishedTree, "invoices")
        invoices.mkdir()
        File(invoices, "note.txt").writeText("note")

        val minutesAttributes = Files.readAttributes(
            minutes.toPath(),
            BasicFileAttributes::class.java,
            LinkOption.NOFOLLOW_LINKS
        )

        go(publishedTree.absolutePath, 1)

        val rootHtml = File(publishedTree, "index.html").readText(Charsets.UTF_8)
        val invoicesHtml = File(invoices, "index.html").readText(Charsets.UTF_8)

        assertEquals(11L, minutes.length())
        assertTrue(rootHtml.contains("href=\"./minutes.txt\""))
        assertTrue(rootHtml.contains("title=\"${isolate_bidi_plain_text("minutes.txt")} 파일\""))
        assertTrue(rootHtml.contains("<span class=\"entry-size\">11 B</span>"))
        assertTrue(
            rootHtml.contains(
                """<time class="entry-mtime" datetime="${format_iso_instant(minutesAttributes.lastModifiedTime().toMillis())}""""
            )
        )
        assertTrue(rootHtml.contains("<span class=\"entry-name\" dir=\"auto\">تقرير.pdf</span>"))
        assertTrue(rootHtml.contains("title=\"${isolate_bidi_plain_text("تقرير.pdf")} 파일\""))
        assertTrue(rootHtml.contains("href=\"./${arabicReport.name.urlEncodePath()}\""))
        assertTrue(rootHtml.contains("<h1 dir=\"auto\">"))
        assertFalse(rootHtml.contains("scratch.tmp"))
        assertFalse(rootHtml.contains(".env"))
        assertFalse(rootHtml.contains("href=\"./..\""))
        assertFalse(rootHtml.contains("상위 디렉토리로 이동"))

        assertTrue(invoicesHtml.contains("href=\"./..\""))
        assertTrue(invoicesHtml.contains("<span class=\"visually-hidden\">상위 디렉토리로 이동</span>"))
        assertTrue(invoicesHtml.contains("href=\"./note.txt\""))
        assertTrue(invoicesHtml.contains("<span class=\"entry-name\" dir=\"auto\">note.txt</span>"))
    }
}
