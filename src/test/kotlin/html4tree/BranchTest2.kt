package html4tree

import org.junit.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import java.io.File

class BranchTest2 {
    @Test
    fun testIndexMiddleBranch() {
        val tempDir = createTempDir("testIndexMiddle")
        try {
            // Create .html4ignore explicitly excluding index.html to see branch behavior
            File(tempDir, ".html4ignore").writeText("index\\.html")
            File(tempDir, "regular_file.txt").writeText("test")
            val subDir = File(tempDir, "subDir")
            subDir.mkdir()

            process_dir(tempDir)

            val indexContent = File(tempDir, "index.html").readText()
            assertTrue(indexContent.contains("regular_file.txt"))
            assertTrue(indexContent.contains("subDir"))
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
