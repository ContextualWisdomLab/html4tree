package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.nio.file.Files

class MainTest {

    @Test
    fun testEscapeHtml() {
        assertEquals("&amp;", "&".escapeHtml())
        assertEquals("&lt;", "<".escapeHtml())
        assertEquals("&gt;", ">".escapeHtml())
        assertEquals("&quot;", "\"".escapeHtml())
        assertEquals("&#x27;", "'".escapeHtml())
        assertEquals("&amp;&lt;&gt;&quot;&#x27;", "&<>\"'".escapeHtml())
        assertEquals("normal text", "normal text".escapeHtml())
    }

    @Test
    fun testUrlEncodePath() {
        assertEquals("hello%20world", "hello world".urlEncodePath())
        assertEquals("test%26path", "test&path".urlEncodePath())
    }

    @Test
    fun testProcessIgnoreFile() {
        val tempDir = Files.createTempDirectory("test-ignore").toFile()
        try {
            val ignoreFile = File(tempDir, ".html4ignore")
            ignoreFile.writeText(".*\\.txt\nignored_dir\n")

            val file1 = File(tempDir, "file1.txt")
            file1.createNewFile()
            val file2 = File(tempDir, "file2.doc")
            file2.createNewFile()
            val ignoredDir = File(tempDir, "ignored_dir")
            ignoredDir.mkdir()
            val normalDir = File(tempDir, "normal_dir")
            normalDir.mkdir()

            val exclude = process_ignore_file(tempDir)

            assertTrue(exclude.contains("file1.txt"))
            assertTrue(exclude.contains("ignored_dir"))
            assertFalse(exclude.contains("file2.doc"))
            assertFalse(exclude.contains("normal_dir"))
            assertTrue(exclude.contains("index.html")) // Always added
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testProcessIgnoreFileNoIgnoreFile() {
        val tempDir = Files.createTempDirectory("test-no-ignore").toFile()
        try {
            val file2 = File(tempDir, "file2.doc")
            file2.createNewFile()

            val exclude = process_ignore_file(tempDir)

            assertEquals(1, exclude.size)
            assertTrue(exclude.contains("index.html")) // Always added
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testProcessDir() {
        val tempDir = Files.createTempDirectory("test-process").toFile()
        try {
            val file1 = File(tempDir, "file1.txt")
            file1.createNewFile()
            val dir1 = File(tempDir, "dir1")
            dir1.mkdir()

            process_dir(tempDir)

            val indexFile = File(tempDir, "index.html")
            assertTrue(indexFile.exists())

            val content = indexFile.readText()
            assertTrue(content.contains("<title>${tempDir.name.escapeHtml()}</title>"))
            assertTrue(content.contains("file1.txt"))
            assertTrue(content.contains("dir1"))
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testGo() {
        val tempDir = Files.createTempDirectory("test-go").toFile()
        try {
            val subDir1 = File(tempDir, "sub1")
            subDir1.mkdir()
            val subDir2 = File(subDir1, "sub2")
            subDir2.mkdir()
            val file1 = File(subDir2, "file1.txt")
            file1.createNewFile()

            go(tempDir.absolutePath, -1)

            assertTrue(File(tempDir, "index.html").exists())
            assertTrue(File(subDir1, "index.html").exists())
            assertTrue(File(subDir2, "index.html").exists())
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testGoWithMaxLevel() {
        val tempDir = Files.createTempDirectory("test-go-max").toFile()
        try {
            val subDir1 = File(tempDir, "sub1")
            subDir1.mkdir()
            val subDir2 = File(subDir1, "sub2")
            subDir2.mkdir()

            go(tempDir.absolutePath, 0) // Only top level

            assertTrue(File(tempDir, "index.html").exists())
            assertFalse(File(subDir1, "index.html").exists())
            assertFalse(File(subDir2, "index.html").exists())
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testMain() {
        val tempDir = Files.createTempDirectory("test-main").toFile()
        try {
            main(arrayOf(tempDir.absolutePath))
            assertTrue(File(tempDir, "index.html").exists())
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGoInvalidDir() {
        go("non_existent_directory_for_test", -1)
    }

    @Test
    fun testHelp() {
        help()
    }
}
