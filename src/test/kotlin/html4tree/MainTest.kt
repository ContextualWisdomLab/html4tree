package html4tree

import org.junit.Test
import org.junit.After
import org.junit.Before
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import java.io.File
import java.nio.file.Files
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class MainTest {
    private lateinit var tempDir: File

    @Before
    fun setup() {
        tempDir = Files.createTempDirectory("html4tree_test").toFile()
    }

    @After
    fun teardown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun testEscapeHtml() {
        assertEquals("&amp;&lt;&gt;&quot;&#x27;", "&<>\"'".escapeHtml())
        assertEquals("normal text", "normal text".escapeHtml())
    }

    @Test
    fun testUrlEncodePath() {
        assertEquals("spaced%20path", "spaced path".urlEncodePath())
        assertEquals("normal_path", "normal_path".urlEncodePath())
        assertEquals("path%2Fwith%2Fslash", "path/with/slash".urlEncodePath())
    }

    @Test
    fun testHelp() {
        val outContent = ByteArrayOutputStream()
        val originalOut = System.out
        System.setOut(PrintStream(outContent))
        try {
            help()
            assertEquals("ERROR: help has not been written yet!\n", outContent.toString())
        } finally {
            System.setOut(originalOut)
        }
    }

    @Test
    fun testProcessIgnoreFile() {
        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText(".*\\.txt\n.*\\.log")

        val txtFile = File(tempDir, "test.txt")
        txtFile.createNewFile()

        val logFile = File(tempDir, "test.log")
        logFile.createNewFile()

        val normalFile = File(tempDir, "test.md")
        normalFile.createNewFile()

        val excluded = process_ignore_file(tempDir)

        assertTrue(excluded.contains("test.txt"))
        assertTrue(excluded.contains("test.log"))
        assertTrue(excluded.contains("index.html"))
        assertFalse(excluded.contains("test.md"))
    }

    @Test
    fun testProcessIgnoreFileNoIgnore() {
        val excluded = process_ignore_file(tempDir)
        assertTrue(excluded.contains("index.html"))
        assertEquals(1, excluded.size)
    }

    @Test
    fun testProcessDir() {
        val subdir = File(tempDir, "subdir")
        subdir.mkdir()

        val file1 = File(tempDir, "file1.txt")
        file1.createNewFile()

        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText(".*\\.ignore")

        val ignoredFile = File(tempDir, "test.ignore")
        ignoredFile.createNewFile()

        process_dir(tempDir)

        val indexFile = File(tempDir, "index.html")
        assertTrue(indexFile.exists())

        val htmlContent = indexFile.readText()
        assertTrue(htmlContent.contains("<html lang=\"ko\">"))
        assertTrue(htmlContent.contains("aria-label=\"상위 디렉토리로 이동\""))
        assertTrue(htmlContent.contains("file1.txt"))
        assertTrue(htmlContent.contains("subdir"))
    }

    @Test
    fun testGo() {
        val subdir1 = File(tempDir, "dir1")
        subdir1.mkdir()

        val subdir2 = File(subdir1, "dir2")
        subdir2.mkdir()

        go(tempDir.absolutePath, 1) // maxLevel 1

        assertTrue(File(tempDir, "index.html").exists())
        assertTrue(File(subdir1, "index.html").exists())
        assertFalse(File(subdir2, "index.html").exists()) // level 2, maxLevel 1

        val subdir3 = File(tempDir, "dir3")
        subdir3.mkdir()
        go(tempDir.absolutePath, -1) // maxLevel -1 (infinite)
        assertTrue(File(subdir3, "index.html").exists())
    }

    @Test
    fun testGoRequireDir() {
        val file = File(tempDir, "notadir.txt")
        file.createNewFile()

        var exceptionThrown = false
        try {
            go(file.absolutePath, -1)
        } catch (e: IllegalArgumentException) {
            exceptionThrown = true
        }
        assertTrue(exceptionThrown)
    }

    @Test
    fun testHtml4treeClikt() {
        val cmd = Html4tree()
        cmd.parse(listOf("--max-level", "2", tempDir.absolutePath))
        assertEquals(2, cmd.maxLevel)
        assertEquals(tempDir.absolutePath, cmd.topDir)
    }

    @Test
    fun testProcessDirSubdirHtml() {
        val subdir = File(tempDir, "subdir")
        subdir.mkdir()
        process_dir(tempDir)
        val indexFile = File(tempDir, "index.html")
        val htmlContent = indexFile.readText()
        assertTrue(htmlContent.contains("subdir/"))
        assertTrue(htmlContent.contains("&#128193;"))
    }

}
