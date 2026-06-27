package html4tree

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import java.io.File

class CoreLogicTest {

    @Test
    fun testProcessIgnoreFile() {
        val tempDir = createTempDir("testDir")
        try {
            // No ignore file
            val excluded1 = process_ignore_file(tempDir)
            assertEquals(1, excluded1.size)
            assertTrue(excluded1.contains("index.html"))

            // With ignore file
            File(tempDir, ".html4ignore").writeText(".*\\.txt\n.*\\.log")
            File(tempDir, "file1.txt").writeText("test")
            File(tempDir, "file2.log").writeText("test")
            File(tempDir, "file3.md").writeText("test")

            val excluded2 = process_ignore_file(tempDir)
            assertTrue(excluded2.contains("index.html"))
            assertTrue(excluded2.contains("file1.txt"))
            assertTrue(excluded2.contains("file2.log"))
            assertFalse(excluded2.contains("file3.md"))
            assertFalse(excluded2.contains(".html4ignore")) // not explicitly ignored in the file

        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testProcessDirAndGo() {
        val tempDir = createTempDir("testGoDir")
        try {
            // Create some structure
            File(tempDir, "file1.txt").writeText("test")
            val subDir1 = File(tempDir, "subDir1")
            subDir1.mkdir()
            File(subDir1, "file2.txt").writeText("test")
            val subDir2 = File(subDir1, "subDir2")
            subDir2.mkdir()
            File(subDir2, "file3.txt").writeText("test")

            // Test go with maxLevel 0
            go(tempDir.absolutePath, 0)
            assertTrue(File(tempDir, "index.html").exists())
            assertFalse(File(subDir1, "index.html").exists())
            assertFalse(File(subDir2, "index.html").exists())

            // check contents of generated file for maxLevel 0
            val indexContent = File(tempDir, "index.html").readText()
            assertTrue(indexContent.contains("<html lang=\"en\">"))
            assertTrue(indexContent.contains("<meta charset=\"utf-8\">"))
            assertTrue(indexContent.contains("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"))
            assertTrue(indexContent.contains("file1.txt"))
            assertTrue(indexContent.contains("subDir1"))
            assertFalse(indexContent.contains(".html4ignore"))

            // delete index.html
            File(tempDir, "index.html").delete()

            // Test go with maxLevel 1
            go(tempDir.absolutePath, 1)
            assertTrue(File(tempDir, "index.html").exists())
            assertTrue(File(subDir1, "index.html").exists())
            assertFalse(File(subDir2, "index.html").exists())

            File(tempDir, "index.html").delete()
            File(subDir1, "index.html").delete()

            // Test go with maxLevel -1 (unlimited)
            go(tempDir.absolutePath, -1)
            assertTrue(File(tempDir, "index.html").exists())
            assertTrue(File(subDir1, "index.html").exists())
            assertTrue(File(subDir2, "index.html").exists())

        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testMainAndHtml4Tree() {
        val tempDir = createTempDir("testMainDir")
        try {
            File(tempDir, "file1.txt").writeText("test")
            val subDir1 = File(tempDir, "subDir1")
            subDir1.mkdir()

            // Run main
            main(arrayOf("--max-level", "0", tempDir.absolutePath))
            assertTrue(File(tempDir, "index.html").exists())
            assertFalse(File(subDir1, "index.html").exists())
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
