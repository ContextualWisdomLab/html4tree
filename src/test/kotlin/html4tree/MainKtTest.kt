package html4tree

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull
import java.io.File
import org.junit.Rule
import org.junit.rules.TemporaryFolder

class MainKtTest {

    @Rule
    @JvmField
    val tempFolder = TemporaryFolder()

    @Test
    fun testEscapeHtml() {
        assertEquals("&amp;", "&".escapeHtml())
        assertEquals("&lt;", "<".escapeHtml())
        assertEquals("&gt;", ">".escapeHtml())
        assertEquals("&quot;", "\"".escapeHtml())
        assertEquals("&#x27;", "'".escapeHtml())
        assertEquals("test", "test".escapeHtml())
    }

    @Test
    fun testUrlEncodePath() {
        assertEquals("hello%20world", "hello world".urlEncodePath())
        assertEquals("hello%2Bworld", "hello+world".urlEncodePath())
        assertEquals("%2Fhello%2Fworld", "/hello/world".urlEncodePath())
    }

    @Test
    fun testProcessIgnoreFileNoIgnore() {
        val dir = tempFolder.newFolder("testdir")
        val exclude = process_ignore_file(dir)
        assertEquals(listOf("index.html"), exclude)
    }

    @Test
    fun testProcessIgnoreFileWithIgnore() {
        val dir = tempFolder.newFolder("testdir2")
        File(dir, "file1.txt").createNewFile()
        File(dir, "file2.md").createNewFile()
        val ignoreFile = File(dir, ".html4ignore")
        ignoreFile.writeText(".*\\.txt")

        val exclude = process_ignore_file(dir)
        assertTrue(exclude.contains("index.html"))
        assertTrue(exclude.contains("file1.txt"))
        assertFalse(exclude.contains("file2.md"))
    }

    @Test
    fun testProcessDir() {
        val dir = tempFolder.newFolder("processdirtest")
        File(dir, "a.txt").createNewFile()
        val subdir = File(dir, "subdir")
        subdir.mkdir()
        File(dir, ".html4ignore").writeText(".*\\.txt")

        process_dir(dir)

        val indexFile = File(dir, "index.html")
        assertTrue(indexFile.exists())
        val content = indexFile.readText()
        assertTrue(content.contains("<!doctype html>"))
        assertTrue(content.contains("<html lang=\"ko\">"))
        assertTrue(content.contains("subdir/"))
        assertTrue(content.contains("aria-label=\"Directory: subdir\""))
        assertFalse(content.contains("a.txt")) // ignored
    }

    @Test
    fun testGo() {
        val root = tempFolder.newFolder("root")
        val subdir1 = File(root, "subdir1")
        subdir1.mkdir()
        val subdir2 = File(subdir1, "subdir2")
        subdir2.mkdir()

        go(root.absolutePath, -1)

        assertTrue(File(root, "index.html").exists())
        assertTrue(File(subdir1, "index.html").exists())
        assertTrue(File(subdir2, "index.html").exists())
    }

    @Test
    fun testGoMaxLevel() {
        val root = tempFolder.newFolder("root_max")
        val subdir1 = File(root, "subdir1")
        subdir1.mkdir()
        val subdir2 = File(subdir1, "subdir2")
        subdir2.mkdir()

        go(root.absolutePath, 0)

        assertTrue(File(root, "index.html").exists())
        assertFalse(File(subdir1, "index.html").exists())
        assertFalse(File(subdir2, "index.html").exists())
    }

    @Test
    fun testHelp() {
        // help() just prints to standard output, we just call it to cover it
        help()
    }

    @Test
    fun testCliCommand() {
        val root = tempFolder.newFolder("cli_test")
        val cmd = Html4tree()
        cmd.parse(arrayOf("--max-level", "0", root.absolutePath))
        assertTrue(File(root, "index.html").exists())
    }

    @Test
    fun testMainArgs() {
        val root = tempFolder.newFolder("main_args_test")
        html4tree.main(arrayOf(root.absolutePath))
        assertTrue(File(root, "index.html").exists())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGoRequireFileExists() {
        go("does_not_exist_xyz", -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGoRequireFileIsDirectory() {
        val f = tempFolder.newFile("not_a_dir.txt")
        go(f.absolutePath, -1)
    }

    @Test
    fun testGoWithNonDirectoryInside() {
        val root = tempFolder.newFolder("root_mixed")
        File(root, "file1.txt").createNewFile()
        val subdir = File(root, "subdir1")
        subdir.mkdir()
        File(subdir, "file2.txt").createNewFile()

        go(root.absolutePath, -1)

        assertTrue(File(root, "index.html").exists())
        assertTrue(File(subdir, "index.html").exists())
    }

    @Test
    fun testPullReturnsPushedEntry() {
        val root = tempFolder.newFolder("root_for_pull")
        val ll = LinkedList()
        ll.push(LinkedListEntry(root, 0))
        val pulled = ll.pull()
        assertEquals(root, pulled?.file)
        assertEquals(0, pulled?.level)
        assertNull(ll.pull())
    }
}
