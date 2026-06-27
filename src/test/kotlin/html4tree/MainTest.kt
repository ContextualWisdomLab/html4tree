package html4tree

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import java.io.File
import org.junit.After
import org.junit.Before

class MainTest {

    val tempDir = File("temp_test_dir")

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
        assertEquals("&amp;&lt;&gt;&quot;&#x27;", "&<>\"'".escapeHtml())
    }

    @Test
    fun testUrlEncodePath() {
        assertEquals("test%20dir", "test dir".urlEncodePath())
    }

    @Test
    fun testGo() {
        File(tempDir, "file1.txt").createNewFile()
        val subDir = File(tempDir, "subdir")
        subDir.mkdir()
        File(subDir, "file2.txt").createNewFile()

        go(tempDir.absolutePath, -1)
        assertTrue(File(tempDir, "index.html").exists())
        assertTrue(File(subDir, "index.html").exists())
    }

    @Test
    fun testGoWithMaxLevel() {
        File(tempDir, "file1.txt").createNewFile()
        val subDir = File(tempDir, "subdir")
        subDir.mkdir()
        File(subDir, "file2.txt").createNewFile()

        go(tempDir.absolutePath, 0)
        assertTrue(File(tempDir, "index.html").exists())
        assertFalse(File(subDir, "index.html").exists())
    }

    @Test
    fun testProcessIgnoreFile() {
        File(tempDir, "file1.txt").createNewFile()
        File(tempDir, "file2.png").createNewFile()
        File(tempDir, ".html4ignore").writeText(".*\\.txt")

        val excluded = process_ignore_file(tempDir)
        assertTrue(excluded.contains("file1.txt"))
        assertFalse(excluded.contains("file2.png"))
        assertTrue(excluded.contains("index.html")) // always included
    }

    @Test
    fun testHtml4treeCommand() {
        val cmd = Html4tree()
        cmd.parse(listOf("--max-level", "0", tempDir.absolutePath))
        assertTrue(File(tempDir, "index.html").exists())
    }

    @Test
    fun testMain() {
        main(arrayOf("--max-level", "0", tempDir.absolutePath))
        assertTrue(File(tempDir, "index.html").exists())
    }

    @Test
    fun testProcessDir() {
        File(tempDir, "file1.txt").createNewFile()
        val subDir = File(tempDir, "subdir")
        subDir.mkdir()
        process_dir(tempDir)

        val indexFile = File(tempDir, "index.html")
        assertTrue(indexFile.exists())
        val content = indexFile.readText()
        assertTrue(content.contains("file1.txt"))
        assertTrue(content.contains("subdir"))
    }

    @Test
    fun testHelp() {
        help()
    }
}
