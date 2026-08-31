package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertTrue

class BidiDirectionTest {
    @Test
    fun testProcessDirAppliesAutoDirectionToFileNames() {
        val tempDir = Files.createTempDirectory("html4tree-bidi-").toFile()
        try {
            File(tempDir, "file1.txt").createNewFile()

            process_dir(tempDir)

            val htmlContent = File(tempDir, "index.html").readText()
            assertTrue(htmlContent.contains("<span dir=\"auto\">file1.txt</span>"))
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
