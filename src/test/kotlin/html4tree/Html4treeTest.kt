package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.io.File

class Html4treeTest {
    @Test
    fun testProcessIgnoreFile() {
        val tempDir = File.createTempFile("temp", "dir")
        tempDir.delete()
        tempDir.mkdir()

        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText(".*\\.txt\n.*\\.log")

        val file1 = File(tempDir, "test1.txt")
        file1.createNewFile()
        val file2 = File(tempDir, "test2.log")
        file2.createNewFile()
        val file3 = File(tempDir, "test3.csv")
        file3.createNewFile()

        val excludes = process_ignore_file(tempDir)

        System.err.println("Excludes: " + excludes)

        assertTrue(excludes.contains("test1.txt"))
        assertTrue(excludes.contains("test2.log"))
        assertFalse(excludes.contains("test3.csv"))
        assertTrue(excludes.contains("index.html"))

        tempDir.deleteRecursively()
    }
}
