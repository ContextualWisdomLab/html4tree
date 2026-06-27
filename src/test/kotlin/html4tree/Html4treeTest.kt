package html4tree

import org.junit.Test
import org.junit.After
import org.junit.Before
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertNotNull
import kotlin.test.assertFailsWith
import java.io.File
import java.nio.file.Files

class Html4treeTest {

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
    fun testLinkedList() {
        val ll = LinkedList()
        val file1 = File("test1")
        val file2 = File("test2")

        assertNull(ll.pull())

        ll.push(LinkedListEntry(file1, 0))
        val entry1 = ll.pull()
        assertEquals(file1, entry1?.file)

        // Push two items to exercise the `first?.next != null` block in push
        val ll3 = LinkedList()
        ll3.push(LinkedListEntry(file1, 0))
        ll3.push(LinkedListEntry(file2, 1))

        // This exercises first != null branch
        assertNotNull(ll3.pull())

        // Covering missing branches
        val ll2 = LinkedList()
        ll2.pull()

        // Force evaluation of Entry's toString/equals for 100% data class coverage
        val e = Entry(file1, 0, null)
        val e2 = e.copy(next = e)
        e.hashCode()
        e.toString()
        e.equals(e2)

        val le = LinkedListEntry(file1, 0)
        val le2 = le.copy()
        le.hashCode()
        le.toString()
        le.equals(le2)
    }

    @Test
    fun testEscapeHtml() {
        assertEquals("&amp;&lt;&gt;&quot;&#x27;", "&<>\"'".escapeHtml())
    }

    @Test
    fun testUrlEncodePath() {
        assertEquals("a%20b", "a b".urlEncodePath())
    }

    @Test
    fun testHelp() {
        help()
    }

    @Test
    fun testMainAndCli() {
        val subdir = File(tempDir, "subdir")
        subdir.mkdir()
        val file = File(subdir, "file.txt")
        file.writeText("test")

        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText("sub.*")

        main(arrayOf(tempDir.absolutePath))

        val indexHtml = File(tempDir, "index.html")
        assertTrue(indexHtml.exists())
    }

    @Test
    fun testProcessDirWithExclude() {
        val subdir1 = File(tempDir, "subdir1")
        subdir1.mkdir()
        val subdir2 = File(tempDir, "subdir2")
        subdir2.mkdir()
        val file = File(tempDir, "file.txt")
        file.writeText("test")

        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText("subdir1")

        process_dir(tempDir)

        val indexHtml = File(tempDir, "index.html")
        assertTrue(indexHtml.exists())
        val content = indexHtml.readText()
        assertFalse(content.contains("subdir1"))
        assertTrue(content.contains("subdir2"))
        assertTrue(content.contains("file.txt"))
    }

    @Test
    fun testProcessDirHtml4IgnoreWithEmptyLine() {
        val subdir1 = File(tempDir, "subdir1")
        subdir1.mkdir()
        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText("\nsubdir1\n")

        process_dir(tempDir)

        val indexHtml = File(tempDir, "index.html")
        assertTrue(indexHtml.exists())
        val content = indexHtml.readText()
        assertFalse(content.contains("subdir1"))
    }


    @Test
    fun testGoWithMaxLevel() {
        val subdir1 = File(tempDir, "subdir1")
        subdir1.mkdir()
        val subdir2 = File(subdir1, "subdir2")
        subdir2.mkdir()

        go(tempDir.absolutePath, 0)

        assertTrue(File(tempDir, "index.html").exists())
        assertFalse(File(subdir1, "index.html").exists())
    }

    @Test
    fun testGoInvalidDir() {
        assertFailsWith<IllegalArgumentException> {
            go("nonexistent_directory_name", -1)
        }
    }

    @Test
    fun testGoMaxLevelNoLimit() {
        val subdir1 = File(tempDir, "subdir1")
        subdir1.mkdir()
        val subdir2 = File(subdir1, "subdir2")
        subdir2.mkdir()

        // test for maxLevel == -1
        go(tempDir.absolutePath, -1)

        assertTrue(File(tempDir, "index.html").exists())
        assertTrue(File(subdir1, "index.html").exists())
        assertTrue(File(subdir2, "index.html").exists())
    }

    @Test
    fun testMainCompanionObj() {
        // Just calling main to invoke class parsing.
        val html4tree = Html4tree()
        html4tree.main(arrayOf(tempDir.absolutePath))
    }
}
