package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertNotNull

class AppTest {

    @Test
    fun testEscapeHtml() {
        assertEquals("&amp;&lt;&gt;&quot;&#x27;", "&<>\"'".escapeHtml())
    }

    @Test
    fun testUrlEncodePath() {
        assertEquals("hello%20world", "hello world".urlEncodePath())
    }

    @Test
    fun testProcessIgnoreFile() {
        val dir = File("test_ignore_dir")
        dir.mkdirs()
        File(dir, "keep.txt").createNewFile()
        File(dir, "ignore_me.txt").createNewFile()
        val ignoreFile = File(dir, ".html4ignore")
        ignoreFile.writeText(".*ignore_me\\.txt\n")

        val excluded = process_ignore_file(dir)
        assertTrue("index.html" in excluded)
        assertTrue("ignore_me.txt" in excluded)
        assertFalse("keep.txt" in excluded)

        dir.deleteRecursively()
    }

    @Test
    fun testProcessIgnoreFileNoIgnore() {
        val dir = File("test_no_ignore_dir")
        dir.mkdirs()
        val excluded = process_ignore_file(dir)
        assertTrue("index.html" in excluded)
        dir.deleteRecursively()
    }

    @Test
    fun testGo() {
        val dir = File("test_go_dir")
        dir.mkdirs()
        val sub1 = File(dir, "sub1")
        sub1.mkdirs()
        val sub2 = File(sub1, "sub2")
        sub2.mkdirs()

        go(dir.path, 1)

        assertTrue(File(dir, "index.html").exists())
        assertTrue(File(sub1, "index.html").exists())
        assertFalse(File(sub2, "index.html").exists()) // level 2 > maxLevel 1

        dir.deleteRecursively()
    }

    @Test
    fun testGoInfinite() {
        val dir = File("test_go_inf_dir")
        dir.mkdirs()
        val sub1 = File(dir, "sub1")
        sub1.mkdirs()
        go(dir.path, -1)
        assertTrue(File(dir, "index.html").exists())
        assertTrue(File(sub1, "index.html").exists())
        dir.deleteRecursively()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGoInvalidDir() {
        val dir = File("non_existent_dir_123")
        go(dir.path, -1)
    }

    @Test
    fun testHelp() {
        help() // Just for coverage
    }

    @Test
    fun testMain() {
        val dir = File("test_main_dir")
        dir.mkdirs()
        html4tree.main(arrayOf(dir.path, "--max-level", "0"))
        assertTrue(File(dir, "index.html").exists())
        dir.deleteRecursively()
    }

    @Test
    fun testProcessDirSorting() {
        val dir = File("test_sort_dir")
        dir.mkdirs()
        File(dir, "b.txt").createNewFile()
        File(dir, "a.txt").createNewFile()

        process_dir(dir)

        val indexContent = File(dir, "index.html").readText()
        assertTrue(indexContent.indexOf("a.txt") < indexContent.indexOf("b.txt"))

        dir.deleteRecursively()
    }
}
