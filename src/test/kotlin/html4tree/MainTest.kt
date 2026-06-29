package html4tree

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.io.File
import java.nio.file.Files

class MainTest {

    @Test
    fun testProcessIgnoreFile() {
        val tempDir = Files.createTempDirectory("test-ignore").toFile()
        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText(".*\\.txt\n")

        val file1 = File(tempDir, "a.txt")
        file1.createNewFile()
        val file2 = File(tempDir, "b.md")
        file2.createNewFile()

        val excluded = process_ignore_file(tempDir)
        assertTrue(excluded.contains("a.txt"))
        assertTrue(!excluded.contains("b.md"))
        assertTrue(excluded.contains("index.html"))

        tempDir.deleteRecursively()
    }

    @Test
    fun testGoFileInsteadOfDir() {
        val tempFile = File.createTempFile("test", "file")
        try {
            go(tempFile.absolutePath, -1)
            assertTrue(false, "Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // expected
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun testGoWithFileInDir() {
        val tempDir = Files.createTempDirectory("test-go-mixed").toFile()
        val file1 = File(tempDir, "file1.txt")
        file1.createNewFile()
        go(tempDir.absolutePath, -1)
        assertTrue(File(tempDir, "index.html").exists())
        tempDir.deleteRecursively()
    }

    @Test
    fun testProcessIgnoreFileNoFile() {
        val tempDir = Files.createTempDirectory("test-no-ignore").toFile()
        val excluded = process_ignore_file(tempDir)
        assertTrue(excluded.contains("index.html"))
        assertEquals(1, excluded.size)
        tempDir.deleteRecursively()
    }

    @Test
    fun testProcessDir() {
        val tempDir = Files.createTempDirectory("test-process").toFile()
        val subDir = File(tempDir, "subdir")
        subDir.mkdir()
        val file1 = File(tempDir, "file.txt")
        file1.createNewFile()

        process_dir(tempDir)

        val indexHtml = File(tempDir, "index.html")
        assertTrue(indexHtml.exists())
        val content = indexHtml.readText()
        assertTrue(content.contains("subdir"))
        assertTrue(content.contains("file.txt"))
        assertTrue(content.contains("aria-label=\"Directory subdir\""))
        assertTrue(content.contains("aria-label=\"File file.txt\""))

        tempDir.deleteRecursively()
    }

    @Test
    fun testGoMaxLevel() {
        val tempDir = Files.createTempDirectory("test-go").toFile()
        val subDir = File(tempDir, "subdir")
        subDir.mkdir()
        val subSubDir = File(subDir, "subsubdir")
        subSubDir.mkdir()

        go(tempDir.absolutePath, 0)

        assertTrue(File(tempDir, "index.html").exists())
        assertTrue(!File(subDir, "index.html").exists())
        assertTrue(!File(subSubDir, "index.html").exists())

        tempDir.deleteRecursively()
    }

    @Test
    fun testGoNoMaxLevel() {
        val tempDir = Files.createTempDirectory("test-go-all").toFile()
        val subDir = File(tempDir, "subdir")
        subDir.mkdir()

        go(tempDir.absolutePath, -1)

        assertTrue(File(tempDir, "index.html").exists())
        assertTrue(File(subDir, "index.html").exists())

        tempDir.deleteRecursively()
    }

    @Test
    fun testMain() {
        val tempDir = Files.createTempDirectory("test-main").toFile()
        main(arrayOf("--max-level", "0", tempDir.absolutePath))
        assertTrue(File(tempDir, "index.html").exists())
        tempDir.deleteRecursively()
    }

    @Test
    fun testHelp() {
        // Just checking execution doesn't crash since it prints to stdout
        help()
    }
}
