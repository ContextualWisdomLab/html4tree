package html4tree

import org.junit.After
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MainTest {
    private lateinit var tempDir: File

    @Before
    fun setup() {
        tempDir = Files.createTempDirectory("html4tree-test-").toFile()
    }

    @After
    fun teardown() {
        if (tempDir.exists()) {
            tempDir.deleteRecursively()
        }
    }

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

    @Test(expected = IllegalArgumentException::class)
    fun testGoInvalidDir() {
        go("non_existent_directory", -1)
    }

    @Test
    fun testGoEmptyDir() {
        go(tempDir.absolutePath, -1)
        val indexFile = File(tempDir, "index.html")
        assertTrue(indexFile.exists())
        assertTrue(indexFile.readText().contains("<html lang=\"ko\">"))
        assertTrue(indexFile.readText().contains("<main>"))
    }

    @Test
    fun testProcessIgnoreFile() {
        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText(".*\\.txt\n.*\\.log")

        File(tempDir, "test.txt").createNewFile()
        File(tempDir, "test.log").createNewFile()
        File(tempDir, "test.md").createNewFile()

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
        File(tempDir, "file1.txt").createNewFile()
        File(tempDir, "test.ignore").createNewFile()
        File(tempDir, ".html4ignore").writeText(".*\\.ignore")

        process_dir(tempDir)

        val indexFile = File(tempDir, "index.html")
        assertTrue(indexFile.exists())
        val htmlContent = indexFile.readText()
        assertTrue(htmlContent.contains("<html lang=\"ko\">"))
        assertTrue(htmlContent.contains("<main>"))
        assertTrue(htmlContent.contains("</main>"))
        assertTrue(htmlContent.contains("aria-label=\"상위 디렉토리로 이동\""))
        assertTrue(htmlContent.contains("file1.txt"))
        assertTrue(htmlContent.contains("subdir/"))
        assertTrue(htmlContent.contains("&#128193;"))
        assertTrue(htmlContent.contains("<span aria-hidden=\"true\">"))
        assertFalse(htmlContent.contains("test.ignore"))
    }

    @Test
    fun testGoWithSymlink() {
        val subdir = File(tempDir, "subdir")
        subdir.mkdir()

        val targetDir = Files.createTempDirectory("html4tree-target-").toFile()
        File(targetDir, "secret.txt").writeText("secret")

        try {
            val symlink = File(subdir, "symlink")
            try {
                Files.createSymbolicLink(symlink.toPath(), targetDir.absoluteFile.toPath())
            } catch (e: Exception) {
                Assume.assumeTrue("Symlink creation not supported in this environment", false)
            }

            go(tempDir.absolutePath, -1)

            assertTrue(File(tempDir, "index.html").exists())

            val subdirIndex = File(subdir, "index.html")
            assertTrue(subdirIndex.exists())
            assertFalse(subdirIndex.readText().contains("symlink"), "Symlinked directory should not be listed in index.html")

            val symlinkIndex = File(targetDir, "index.html")
            assertFalse(symlinkIndex.exists(), "Symlink target should not have an index.html generated")
        } finally {
            targetDir.deleteRecursively()
        }
    }

    @Test
    fun testGoWithMaxLevel() {
        val subdir = File(tempDir, "subdir")
        subdir.mkdir()
        val subsubdir = File(subdir, "subsubdir")
        subsubdir.mkdir()

        go(tempDir.absolutePath, 0)

        assertTrue(File(tempDir, "index.html").exists())
        assertFalse(File(subdir, "index.html").exists())
        assertFalse(File(subsubdir, "index.html").exists())
    }

    @Test
    fun testGoWithUnreadableDir() {
        val unreadableDir = File(tempDir, "unreadable")
        unreadableDir.mkdir()
        unreadableDir.setWritable(true)
        unreadableDir.setExecutable(true)

        try {
            Assume.assumeTrue(unreadableDir.setReadable(false, false))
            assertNull(unreadableDir.listFiles())

            go(tempDir.absolutePath, -1)

            assertTrue(File(tempDir, "index.html").exists())
            assertTrue(File(unreadableDir, "index.html").exists())
        } finally {
            unreadableDir.setReadable(true, false)
            unreadableDir.setWritable(true, false)
            unreadableDir.setExecutable(true, false)
        }
    }

    @Test
    fun testCliParsing() {
        val cli = Html4tree()
        cli.parse(arrayOf("--max-level", "2", tempDir.absolutePath))
        assertEquals(2, cli.maxLevel)
        assertEquals(tempDir.absolutePath, cli.topDir)
    }

    @Test
    fun testCliMainParsing() {
        val cli = Html4tree()
        cli.parse(arrayOf(tempDir.absolutePath))
        main(arrayOf(tempDir.absolutePath))
        assertTrue(File(tempDir, "index.html").exists())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGoNotADir() {
        val notADir = File(tempDir, "not_a_dir.txt")
        notADir.writeText("test")
        go(notADir.absolutePath, -1)
    }

    @Test
    fun testProcessIgnoreFileWithIndexHtml() {
        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText("index\\.html")
        File(tempDir, "index.html").writeText("existing")
        val excluded = process_ignore_file(tempDir)
        assertTrue(excluded.contains("index.html"))
    }

    @Test
    fun testProcessDirItEqualsCurrDir() {
        File(tempDir, "tempDir").mkdir()
        process_dir(tempDir)
    }
}
