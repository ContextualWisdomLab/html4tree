package html4tree

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class MainTest {

    private val tempDir = File("test_temp_dir")

    @Before
    fun setup() {
        tempDir.mkdir()
    }

    @After
    fun teardown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun testEscapeHtml() {
        assertEquals("&amp;", "&".escapeHtml())
        assertEquals("&lt;", "<".escapeHtml())
        assertEquals("&gt;", ">".escapeHtml())
        assertEquals("&quot;", "\"".escapeHtml())
        assertEquals("&#x27;", "'".escapeHtml())
    }

    @Test
    fun testUrlEncodePath() {
        assertEquals("hello%20world", "hello world".urlEncodePath())
    }

    @Test
    fun testHelp() {
        help() // Just for coverage
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
    }

    @Test
    fun testProcessIgnoreFile() {
        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText(".*\\.txt")
        val txtFile = File(tempDir, "test.txt")
        txtFile.writeText("test")
        val excludeList = process_ignore_file(tempDir)
        assertTrue(excludeList.contains("test.txt"))
        assertTrue(excludeList.contains("index.html"))
    }

    @Test
    fun testProcessIgnoreFileNoIgnore() {
        val excludeList = process_ignore_file(tempDir)
        assertTrue(excludeList.contains("index.html"))
    }

    @Test
    fun testProcessDir() {
        val subdir = File(tempDir, "subdir")
        subdir.mkdir()
        val txtFile = File(tempDir, "test.txt")
        txtFile.writeText("test")

        process_dir(tempDir)

        val indexFile = File(tempDir, "index.html")
        assertTrue(indexFile.exists())
        val content = indexFile.readText()
        assertTrue(content.contains("subdir"))
        assertTrue(content.contains("test.txt"))
    }

    @Test
    fun testGoWithSymlink() {
        val subdir = File(tempDir, "subdir")
        subdir.mkdir()

        val targetDir = File("test_target_dir")
        targetDir.mkdir()
        val targetFile = File(targetDir, "secret.txt")
        targetFile.writeText("secret")

        try {
            val symlink = File(subdir, "symlink")
            Files.createSymbolicLink(symlink.toPath(), targetDir.absoluteFile.toPath())

            go(tempDir.absolutePath, -1)

            val indexFile = File(tempDir, "index.html")
            assertTrue(indexFile.exists())

            val subdirIndex = File(subdir, "index.html")
            assertTrue(subdirIndex.exists())

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
        val excludeList = process_ignore_file(tempDir)
        assertTrue(excludeList.contains("index.html"))
    }

    @Test

    fun testProcessDirItEqualsCurrDir() {
        val innerDir = File(tempDir, "tempDir")
        innerDir.mkdir()
        process_dir(tempDir)
    }
}
