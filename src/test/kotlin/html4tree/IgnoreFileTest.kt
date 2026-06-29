package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.io.File

class IgnoreFileTest {
    @Test
    fun testProcessIgnoreFile() {
        val tempDir = createTempDir("ignoreTestDir")
        try {
            val file1 = File(tempDir, "file1.txt")
            file1.createNewFile()
            val file2 = File(tempDir, "file2.csv")
            file2.createNewFile()

            var excludeList = process_ignore_file(tempDir)
            assertTrue(excludeList.contains("index.html"))
            assertFalse(excludeList.contains("file1.txt"))
            assertFalse(excludeList.contains("file2.csv"))

            val ignoreFile = File(tempDir, ".html4ignore")
            ignoreFile.writeText(".*\\.txt\n")

            excludeList = process_ignore_file(tempDir)
            assertTrue(excludeList.contains("index.html"))
            assertTrue(excludeList.contains("file1.txt"))
            assertFalse(excludeList.contains("file2.csv"))
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
