package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.nio.file.Files

class MainTest {
    @Test
    fun testEscapeHtml() {
        assertEquals("&amp;&lt;&gt;&quot;&#x27;", "&<>\"'".escapeHtml())
    }

    @Test
    fun testUrlEncodePath() {
        assertEquals("a%20b%2Bc", "a b+c".urlEncodePath())
    }

    @Test
    fun testHtml4tree() {
        val tempDir = Files.createTempDirectory("test-html4tree").toFile()
        val subDir = File(tempDir, "subdir")
        subDir.mkdir()
        val file1 = File(tempDir, "test.txt")
        file1.writeText("test")

        val ignoreFile = File(tempDir, ".html4ignore")
        // include index.html to cover the branch where it's already in exclude list
        ignoreFile.writeText(".*\\.txt\nindex\\.html")

        go(tempDir.absolutePath, -1)

        val indexHtml = File(tempDir, "index.html")
        assertTrue(indexHtml.exists())

        val content = indexHtml.readText()
        assertTrue(content.contains("subdir"))
        assertFalse(content.contains("test.txt"))

        tempDir.deleteRecursively()
    }

    @Test
    fun testHtml4treeLevel() {
        val tempDir = Files.createTempDirectory("test-html4tree").toFile()
        val subDir = File(tempDir, "subdir")
        subDir.mkdir()
        val subSubDir = File(subDir, "subsubdir")
        subSubDir.mkdir()

        go(tempDir.absolutePath, 0)

        assertTrue(File(tempDir, "index.html").exists())
        assertFalse(File(subDir, "index.html").exists())

        tempDir.deleteRecursively()
    }

    @Test
    fun testHtml4treeMissingLevel() {
        // test the while branch `lle != null && lle.file.isDirectory()` where lle.file is a file
        val tempDir = Files.createTempDirectory("test-html4tree").toFile()

        val file1 = File(tempDir, "test.txt")
        file1.writeText("test")

        val subDir = File(tempDir, "subdir")
        subDir.mkdir()

        go(tempDir.absolutePath, 1)

        tempDir.deleteRecursively()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGoNotExists() {
        go("not-exists-dir-12345", -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGoNotDirectory() {
        val tempFile = File.createTempFile("test", ".txt")
        try {
            go(tempFile.absolutePath, -1)
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun testMainArgs() {
        val tempDir = Files.createTempDirectory("test-html4tree").toFile()
        main(arrayOf("--max-level=0", tempDir.absolutePath))
        assertTrue(File(tempDir, "index.html").exists())
        tempDir.deleteRecursively()
    }

    @Test
    fun testHelp() {
        help()
    }

    @Test
    fun testProcessIgnoreFileNoIndex() {
        val tempDir = Files.createTempDirectory("test-html4tree").toFile()
        val exclude = process_ignore_file(tempDir)
        assertTrue("index.html" in exclude)
        tempDir.deleteRecursively()
    }

    @Test
    fun testProcessIgnoreFileWithIndex() {
        val tempDir = Files.createTempDirectory("test-html4tree").toFile()
        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText("index\\.html")
        File(tempDir, "index.html").writeText("dummy")

        val exclude = process_ignore_file(tempDir)
        assertTrue("index.html" in exclude)
        tempDir.deleteRecursively()
    }

    @Test
    fun testProcessDirItEqualsCurrDir() {
        val tempDir = Files.createTempDirectory("test-html4tree").toFile()
        val file = File(tempDir, tempDir.name)
        file.writeText("test")
        process_dir(tempDir)
        tempDir.deleteRecursively()
    }

    @Test
    fun testGoWithFileInQueue() {
        val tempDir = Files.createTempDirectory("test-html4tree").toFile()
        val file = File(tempDir, "test.txt")
        file.writeText("test")
        go(tempDir.absolutePath, 1)
        tempDir.deleteRecursively()
    }

    @Test
    fun testWhileNullLle() {
        val tempDir = Files.createTempDirectory("test-html4tree").toFile()
        go(tempDir.absolutePath, -1)
        tempDir.deleteRecursively()
    }
}
