package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.io.File
import com.github.ajalt.clikt.core.PrintHelpMessage
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class MainTest {

    @Test
    fun testEscapeHtml() {
        assertEquals("&amp;&lt;&gt;&quot;&#x27;", "&<>\"'".escapeHtml())
    }

    @Test
    fun testUrlEncodePath() {
        assertEquals("some%20path", "some path".urlEncodePath())
    }

    @Test
    fun testProcessIgnoreFile() {
        val dir = File.createTempFile("testdir", "")
        dir.delete()
        dir.mkdir()
        dir.deleteOnExit()

        val ignoreFile = File(dir, ".html4ignore")
        ignoreFile.writeText(".*\\.txt")
        ignoreFile.deleteOnExit()

        val file1 = File(dir, "test1.txt")
        file1.createNewFile()
        file1.deleteOnExit()

        val file2 = File(dir, "test2.png")
        file2.createNewFile()
        file2.deleteOnExit()

        val excluded = process_ignore_file(dir)
        assertTrue(excluded.contains("test1.txt"))
        assertFalse(excluded.contains("test2.png"))
        assertTrue(excluded.contains("index.html"))
    }

    @Test
    fun testProcessIgnoreFileNoFile() {
        val dir = File.createTempFile("testdir", "")
        dir.delete()
        dir.mkdir()
        dir.deleteOnExit()

        val file2 = File(dir, "test2.png")
        file2.createNewFile()
        file2.deleteOnExit()

        val excluded = process_ignore_file(dir)
        assertTrue(excluded.contains("index.html"))
        assertFalse(excluded.contains("test2.png"))
    }


    @Test
    fun testProcessDir() {
        val dir = File.createTempFile("testdir", "")
        dir.delete()
        dir.mkdir()
        dir.deleteOnExit()

        val subdir = File(dir, "subdir")
        subdir.mkdir()
        subdir.deleteOnExit()

        val file1 = File(dir, "test1.png")
        file1.createNewFile()
        file1.deleteOnExit()

        process_dir(dir)

        val indexFile = File(dir, "index.html")
        assertTrue(indexFile.exists())
        val content = indexFile.readText()
        assertTrue(content.contains("test1.png"))
        assertTrue(content.contains("subdir"))
    }

    @Test
    fun testGo() {
        val dir = File.createTempFile("testdir", "")
        dir.delete()
        dir.mkdir()
        dir.deleteOnExit()

        val subdir = File(dir, "subdir")
        subdir.mkdir()
        subdir.deleteOnExit()

        go(dir.absolutePath, 0)

        assertTrue(File(dir, "index.html").exists())
        assertFalse(File(subdir, "index.html").exists())

        go(dir.absolutePath, 1)
        assertTrue(File(subdir, "index.html").exists())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGoInvalidDir() {
        val file = File.createTempFile("testdir", "")
        file.deleteOnExit()
        go(file.absolutePath, 0)
    }


    @Test
    fun testHelp() {
        val originalOut = System.out
        val baos = ByteArrayOutputStream()
        System.setOut(PrintStream(baos))
        try {
            help()
            assertEquals("ERROR: help has not been written yet!\n", baos.toString())
        } finally {
            System.setOut(originalOut)
        }
    }

    @Test
    fun testMain() {
        val dir = File.createTempFile("testdir", "")
        dir.delete()
        dir.mkdir()
        dir.deleteOnExit()

        main(arrayOf(dir.absolutePath, "--max-level", "0"))
        assertTrue(File(dir, "index.html").exists())
    }

}
