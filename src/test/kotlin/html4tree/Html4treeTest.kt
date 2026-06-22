package html4tree

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import java.io.File
import java.nio.file.Files
import kotlin.test.assertFailsWith

class Html4treeTest {

    @Test
    fun testLinkedList() {
        val list = LinkedList()

        var pulled = list.pull()
        assertEquals(null, pulled)

        val file1 = File("file1")
        val file2 = File("file2")

        list.push(LinkedListEntry(file1, 0))

        list.first = list.first
        list.last = list.last

        list.push(LinkedListEntry(file2, 1))

        pulled = list.pull()
        assertEquals(file1, pulled?.file)
        assertEquals(0, pulled?.level)

        pulled = list.pull()
        assertEquals(file2, pulled?.file)
        assertEquals(1, pulled?.level)

        pulled = list.pull()
        assertEquals(null, pulled)

        list.push(LinkedListEntry(file1, 0))
        list.push(LinkedListEntry(file2, 1))
        list.push(LinkedListEntry(file2, 1))

        val l = LinkedList()
        l.push(LinkedListEntry(File("a"), 0))
        l.push(LinkedListEntry(File("b"), 1))
        l.first = null
        l.push(LinkedListEntry(File("c"), 2))
    }

    @Test
    fun testEntryCoverage() {
        val entry = Entry(File("test"), 1, null)
        assertEquals(1, entry.level)
        val lEntry = LinkedListEntry(File("test"), 1)
        assertEquals(1, lEntry.level)
        val lEntry2 = lEntry.copy()
        assertEquals(lEntry, lEntry2)
        val entry2 = entry.copy()
        assertEquals(entry, entry2)
    }

    @Test
    fun testGo() {
        val tempDir = Files.createTempDirectory("test-go").toFile()
        val subdir1 = File(tempDir, "subdir1")
        subdir1.mkdir()
        val subdir2 = File(subdir1, "subdir2")
        subdir2.mkdir()
        val file1 = File(subdir1, "file1.txt")
        file1.createNewFile()

        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText("subdir1")

        go(tempDir.absolutePath, 1)

        assertTrue(File(tempDir, "index.html").exists())

        assertFailsWith<IllegalArgumentException> {
            go(File(tempDir, "nonexistent").absolutePath, 1)
        }

        assertFailsWith<IllegalArgumentException> {
            go(ignoreFile.absolutePath, 1)
        }

        val tempDirLimit = Files.createTempDirectory("test-go-limit").toFile()
        val subdirLimit = File(tempDirLimit, "subdirLimit")
        subdirLimit.mkdir()
        File(subdirLimit, "subsubdir").mkdir()
        go(tempDirLimit.absolutePath, 0)
        assertTrue(File(tempDirLimit, "index.html").exists())
        assertFalse(File(subdirLimit, "index.html").exists())
        tempDirLimit.deleteRecursively()

        tempDir.deleteRecursively()
    }

    @Test
    fun testProcessIgnoreFile() {
        val tempDir = Files.createTempDirectory("test-ignore").toFile()
        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText("abc\n.*\\.txt\ndef")

        File(tempDir, "abc").createNewFile()
        File(tempDir, "test.txt").createNewFile()
        File(tempDir, "def").createNewFile()
        File(tempDir, "keep.html").createNewFile()

        val excluded = process_ignore_file(tempDir)
        assertTrue(excluded.contains("abc"))
        assertTrue(excluded.contains("test.txt"))
        assertTrue(excluded.contains("def"))
        assertTrue(excluded.contains("index.html"))
        assertFalse(excluded.contains("keep.html"))

        tempDir.deleteRecursively()
    }

    @Test
    fun testProcessIgnoreFileNoFile() {
        val tempDir = Files.createTempDirectory("test-ignore-no-file").toFile()
        File(tempDir, "abc").createNewFile()
        val excluded = process_ignore_file(tempDir)
        assertFalse(excluded.contains("abc"))
        assertTrue(excluded.contains("index.html"))
        tempDir.deleteRecursively()
    }

    @Test
    fun testProcessDir() {
        val tempDir = Files.createTempDirectory("test-process-dir").toFile()
        File(tempDir, "subdir").mkdir()
        File(tempDir, "test.txt").createNewFile()
        File(tempDir, ".html4ignore").writeText("test.txt")

        process_dir(tempDir)

        val indexContent = File(tempDir, "index.html").readText()
        assertTrue(indexContent.contains("subdir"))
        assertFalse(indexContent.contains("test.txt"))

        tempDir.deleteRecursively()
    }

    @Test
    fun testProcessDirWithIgnoreFileIncluded() {
        val tempDir = Files.createTempDirectory("test-process-dir").toFile()
        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText("abc")

        process_dir(tempDir)
        val indexContent = File(tempDir, "index.html").readText()
        assertTrue(indexContent.contains(".html4ignore"))
        tempDir.deleteRecursively()
    }

    @Test
    fun testProcessDirWithPreExistingIndex() {
        val tempDir = Files.createTempDirectory("test-process-dir").toFile()
        File(tempDir, "index.html").writeText("old")
        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText("index.html")

        process_dir(tempDir)
        val indexContent = File(tempDir, "index.html").readText()
        assertTrue(indexContent.contains("html"))
        tempDir.deleteRecursively()
    }

    @Test
    fun testMain() {
        val tempDir = Files.createTempDirectory("test-main").toFile()
        main(arrayOf("--max-level", "1", tempDir.absolutePath))
        assertTrue(File(tempDir, "index.html").exists())
        tempDir.deleteRecursively()
    }

    @Test
    fun testMainNoMaxLevel() {
        val tempDir = Files.createTempDirectory("test-main").toFile()
        main(arrayOf(tempDir.absolutePath))
        assertTrue(File(tempDir, "index.html").exists())
        tempDir.deleteRecursively()
    }

    @Test
    fun testHelp() {
        help()
    }

    @Test
    fun testEntryToString() {
        val entry = Entry(File("a"), 1, null)
        val str = entry.toString()
        assertTrue(str.contains("a"))
    }

    @Test
    fun testLinkedListEntryToString() {
        val lEntry = LinkedListEntry(File("a"), 1)
        val str = lEntry.toString()
        assertTrue(str.contains("a"))
    }
}
