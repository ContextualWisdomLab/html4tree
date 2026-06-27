package html4tree

import org.junit.Test
import kotlin.test.assertTrue
import java.io.File

class BranchTest {
    @Test
    fun testProcessIgnoreFileNoFiles() {
        // hit branch where directory is empty and doesn't match any ignored files
        val tempDir = createTempDir("emptyDir")
        try {
            File(tempDir, ".html4ignore").writeText(".*\\.txt")
            // dir is empty otherwise
            val excluded = process_ignore_file(tempDir)
            assertTrue(excluded.contains("index.html"))
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testGoWithFilesNoDirs() {
        val tempDir = createTempDir("filesOnlyDir")
        try {
            File(tempDir, "file1.txt").writeText("test")
            File(tempDir, "file2.txt").writeText("test")
            // go will loop but find no subdirectories to push to LinkedList
            go(tempDir.absolutePath, -1)
            assertTrue(File(tempDir, "index.html").exists())
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
