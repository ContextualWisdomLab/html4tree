package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class MainTest {

    @Test
    fun testEscapeHtml() {
        assertEquals("&amp;&lt;&gt;&quot;&#x27;", "&<>\"'".escapeHtml())
    }

    @Test
    fun testUrlEncodePath() {
        assertEquals("hello%20world", "hello world".urlEncodePath())
    }

    @Test
    fun testHelp() {
        help() // Just for coverage
    }

    @Test
    fun testMain() {
        val tempDir = createTempDir("html4tree")
        tempDir.deleteOnExit()

        main(arrayOf(tempDir.absolutePath))

        val indexFile = File(tempDir, "index.html")
        assertTrue(indexFile.exists())
    }

    @Test
    fun testGo() {
        val tempDir = createTempDir("html4tree_go")
        tempDir.deleteOnExit()

        val subDir1 = File(tempDir, "sub1")
        subDir1.mkdir()
        val file1 = File(subDir1, "test.txt")
        file1.writeText("test")

        val subDir2 = File(tempDir, "sub2")
        subDir2.mkdir()

        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText("sub2")

        go(tempDir.absolutePath, 1)

        val index1 = File(tempDir, "index.html")
        assertTrue(index1.exists())

        val index2 = File(subDir1, "index.html")
        assertTrue(index2.exists())

        val content = index1.readText()
        assertTrue(content.contains("sub1"))
        assertTrue(!content.contains("sub2")) // Because it's in .html4ignore

        // test go with max level 0
        val tempDir2 = createTempDir("html4tree_go2")
        tempDir2.deleteOnExit()
        val subDir3 = File(tempDir2, "sub3")
        subDir3.mkdir()

        go(tempDir2.absolutePath, 0)
        assertTrue(File(tempDir2, "index.html").exists())
        assertTrue(!File(subDir3, "index.html").exists())
    }

    @Test
    fun testGoRequireFails() {
        assertFailsWith<IllegalArgumentException> {
            go("nonexistent_directory_which_should_fail", -1)
        }
    }

    @Test
    fun testProcessIgnoreFileEmpty() {
        val tempDir = createTempDir("html4tree_ignore")
        tempDir.deleteOnExit()
        val f = File(tempDir, "test.txt")
        f.writeText("test")

        val excluded = process_ignore_file(tempDir)
        assertEquals(listOf("index.html"), excluded)
    }

    @Test
    fun testProcessDir() {
        val tempDir = createTempDir("html4tree_process")
        tempDir.deleteOnExit()

        val f1 = File(tempDir, "a.txt")
        f1.writeText("hello")

        val d1 = File(tempDir, "b_dir")
        d1.mkdir()

        process_dir(tempDir)

        val index = File(tempDir, "index.html")
        assertTrue(index.exists())

        val content = index.readText()
        assertTrue(content.contains("a.txt"))
        assertTrue(content.contains("b_dir"))
    }
}
