package html4tree

import org.junit.Test
import org.junit.After
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import java.io.File
import java.nio.file.Files
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class MainTest {
    private val tempDir = Files.createTempDirectory("html4tree_test").toFile()

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun testEscapeHtml() {
        assertEquals("&amp;&lt;&gt;&quot;&#x27;", "&<>\"'".escapeHtml())
    }

    @Test
    fun testUrlEncodePath() {
        assertEquals("a%20b", "a b".urlEncodePath())
    }

    @Test
    fun testProcessIgnoreFile() {
        val dir = File(tempDir, "ignore_test")
        dir.mkdir()
        File(dir, ".html4ignore").writeText(".*\\.txt\n.*\\.bak")
        File(dir, "a.txt").createNewFile()
        File(dir, "b.bak").createNewFile()
        File(dir, "c.jpg").createNewFile()

        val excluded = process_ignore_file(dir)
        assertTrue(excluded.contains("a.txt"))
        assertTrue(excluded.contains("b.bak"))
        assertTrue(excluded.contains("index.html"))
        assertFalse(excluded.contains("c.jpg"))
    }

    @Test
    fun testProcessIgnoreFileNoFile() {
        val dir = File(tempDir, "no_ignore_test")
        dir.mkdir()
        val excluded = process_ignore_file(dir)
        assertTrue(excluded.contains("index.html"))
    }

    @Test
    fun testProcessDir() {
        val dir = File(tempDir, "process_dir_test")
        dir.mkdir()
        File(dir, "file1.txt").createNewFile()
        File(dir, "subdir").mkdir()

        process_dir(dir)

        val indexFile = File(dir, "index.html")
        assertTrue(indexFile.exists())

        val content = indexFile.readText()
        assertTrue(content.contains("<html lang=\"en\">"))
        assertTrue(content.contains("<meta charset=\"utf-8\">"))
        assertTrue(content.contains("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"))
        assertTrue(content.contains("file1.txt"))
        assertTrue(content.contains("subdir/"))
    }

    @Test
    fun testGo() {
        val top = File(tempDir, "go_test")
        top.mkdir()
        val subdir1 = File(top, "sub1")
        subdir1.mkdir()
        val subdir2 = File(subdir1, "sub2")
        subdir2.mkdir()

        go(top.absolutePath, 1)

        assertTrue(File(top, "index.html").exists())
        assertTrue(File(subdir1, "index.html").exists())
        assertFalse(File(subdir2, "index.html").exists())

        go(top.absolutePath, -1)
        assertTrue(File(subdir2, "index.html").exists())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGoInvalidDir() {
        go(File(tempDir, "non_existent").absolutePath, 0)
    }

    @Test
    fun testHelp() {
        val outContent = ByteArrayOutputStream()
        val originalOut = System.out
        System.setOut(PrintStream(outContent))
        help()
        assertEquals("ERROR: help has not been written yet!\n", outContent.toString())
        System.setOut(originalOut)
    }

    @Test
    fun testMain() {
        val top = File(tempDir, "main_test")
        top.mkdir()
        main(arrayOf(top.absolutePath, "--max-level", "0"))
        assertTrue(File(top, "index.html").exists())
    }
}