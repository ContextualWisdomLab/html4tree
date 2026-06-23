package html4tree

import org.junit.Test
import org.junit.After
import org.junit.Before
import java.io.File
import java.nio.file.Files
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertEquals
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class MainTest {

    private lateinit var tempDir: File

    @Before
    fun setUp() {
        tempDir = Files.createTempDirectory("html4tree_test").toFile()
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun testGoGeneratesIndexHtml() {
        val subDir = File(tempDir, "subdir")
        subDir.mkdir()
        val file = File(tempDir, "file.txt")
        file.createNewFile()

        go(tempDir.absolutePath, -1)

        val indexFile = File(tempDir, "index.html")
        assertTrue(indexFile.exists())

        val content = indexFile.readText()
        assertTrue(content.contains("<html lang=\"en\">"))
        assertTrue(content.contains("subdir"))
        assertTrue(content.contains("file.txt"))

        val subIndexFile = File(subDir, "index.html")
        assertTrue(subIndexFile.exists())
    }

    @Test
    fun testGoWithMaxLevel() {
        val subDir1 = File(tempDir, "subdir1")
        subDir1.mkdir()
        val subDir2 = File(subDir1, "subdir2")
        subDir2.mkdir()

        go(tempDir.absolutePath, 0)

        assertTrue(File(tempDir, "index.html").exists())
        assertFalse(File(subDir1, "index.html").exists())
        assertFalse(File(subDir2, "index.html").exists())
    }

    @Test
    fun testHtml4Ignore() {
        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText(".*\\.txt")
        val txtFile = File(tempDir, "test.txt")
        txtFile.createNewFile()
        val pngFile = File(tempDir, "test.png")
        pngFile.createNewFile()

        go(tempDir.absolutePath, -1)

        val indexFile = File(tempDir, "index.html")
        val content = indexFile.readText()
        assertFalse(content.contains("test.txt"))
        assertTrue(content.contains("test.png"))
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
    fun testMain() {
        val subDir = File(tempDir, "subdir")
        subDir.mkdir()

        main(arrayOf(tempDir.absolutePath))

        assertTrue(File(tempDir, "index.html").exists())
        assertTrue(File(subDir, "index.html").exists())
    }
}
