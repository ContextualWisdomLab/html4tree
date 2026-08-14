package html4tree

import java.io.File
import java.nio.file.Files
import kotlin.test.assertTrue
import org.junit.Test

class BidiRenderingTest {
    @Test
    fun mixedDirectionNamesReceiveAutomaticDirectionContainers() {
        val parentDirectory = Files.createTempDirectory("bidi_rendering").toFile()
        val indexedDirectory = File(parentDirectory, "دليل-2026").apply { mkdir() }
        val mixedDirectionFile = File(indexedDirectory, "דוח-report.txt").apply {
            writeText("report")
        }

        try {
            process_dir(indexedDirectory)

            val generatedHtml = File(indexedDirectory, "index.html").readText()
            assertTrue(generatedHtml.contains("<h1 dir=\"auto\">دليل-2026</h1>"))
            assertTrue(generatedHtml.contains("<span dir=\"auto\">דוח-report.txt</span>"))
            assertTrue(generatedHtml.contains("aria-label=\"דוח-report.txt 파일\""))
        } finally {
            parentDirectory.deleteRecursively()
        }
    }
}
