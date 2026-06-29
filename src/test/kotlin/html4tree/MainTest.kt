package html4tree

import org.junit.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import java.io.File
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class MainTest {
    @Test
    fun testGoAndProcessDir() {
        val tempDir = createTempDir("html4tree_test")
        val subdir1 = File(tempDir, "subdir1").apply { mkdir() }
        val xssDir = File(tempDir, "<img src=x onerror=alert(1)>").apply { mkdir() }
        val file1 = File(tempDir, "file1.txt").apply { writeText("content") }

        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText("subdir1\n^file1\\.txt$")

        go(tempDir.absolutePath, 1)

        val indexFile = File(tempDir, "index.html")
        assertTrue(indexFile.exists())

        val content = indexFile.readText()
        assertFalse(content.contains("file1.txt"))
        assertFalse(content.contains(">subdir1<"))
        assertTrue(content.contains("&lt;img src=x onerror=alert(1)&gt;"))
        assertFalse(content.contains("<img src=x onerror=alert(1)>"))

        val indexFile2 = File(xssDir, "index.html")
        assertTrue(indexFile2.exists())

        tempDir.deleteRecursively()
    }

    @Test
    fun testMain() {
        val tempDir = createTempDir("html4tree_test_main")
        main(arrayOf("--max-level", "0", tempDir.absolutePath))
        assertTrue(File(tempDir, "index.html").exists())
        tempDir.deleteRecursively()
    }

    @Test
    fun testProcessIgnoreFileWithNoIgnoreFile() {
        val tempDir = createTempDir("html4tree_no_ignore")
        val exclude = process_ignore_file(tempDir)
        assertEquals(1, exclude.size)
        assertEquals("index.html", exclude[0])
        tempDir.deleteRecursively()
    }

    @Test
    fun testHelp() {
        val originalOut = System.out
        val outContent = ByteArrayOutputStream()
        System.setOut(PrintStream(outContent))
        help()
        assertEquals("ERROR: help has not been written yet!\n", outContent.toString())
        System.setOut(originalOut)
    }

    @Test
    fun testGoInfiniteDepth() {
        val tempDir = createTempDir("html4tree_infinite_depth")
        val subdir = File(tempDir, "subdir").apply { mkdir() }
        val subsubdir = File(subdir, "subsubdir").apply { mkdir() }

        go(tempDir.absolutePath, -1)

        assertTrue(File(tempDir, "index.html").exists())
        assertTrue(File(subdir, "index.html").exists())
        assertTrue(File(subsubdir, "index.html").exists())

        tempDir.deleteRecursively()
    }

    @Test
    fun testGoWithNonExistentDir() {
        assertFailsWith<IllegalArgumentException> {
            go("/non/existent/directory/that/should/not/exist/12345", 1)
        }
    }

    @Test
    fun testGoWithFileNotDir() {
        val tempFile = File.createTempFile("html4tree_test_file", ".txt")
        assertFailsWith<IllegalArgumentException> {
            go(tempFile.absolutePath, 1)
        }
        tempFile.delete()
    }
}
