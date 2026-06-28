package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import java.nio.file.Files
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.PrintMessage

class Html4treeTest {

    @Test
    fun testEscapeHtml() {
        assertEquals("&amp;", "&".escapeHtml())
        assertEquals("&lt;", "<".escapeHtml())
        assertEquals("&gt;", ">".escapeHtml())
        assertEquals("&quot;", "\"".escapeHtml())
        assertEquals("&#x27;", "'".escapeHtml())
        assertEquals("a&amp;b&lt;c&gt;d&quot;e&#x27;f", "a&b<c>d\"e'f".escapeHtml())
    }

    @Test
    fun testUrlEncodePath() {
        assertEquals("test%20file", "test file".urlEncodePath())
        assertEquals("test%26file", "test&file".urlEncodePath())
    }

    @Test
    fun testLinkedList() {
        val ll = LinkedList()
        val file1 = File("file1")
        val file2 = File("file2")

        assertNull(ll.pull())

        ll.push(LinkedListEntry(file1, 0))
        ll.push(LinkedListEntry(file2, 1))

        // Add a third file to trigger the first?.next = null case fully if needed, though 2 covers the else branch
        val file3 = File("file3")
        ll.push(LinkedListEntry(file3, 2))

        // Push when first is null but last is not (this is impossible in real usage but possible via properties)
        val ll4 = LinkedList()
        ll4.last = Entry(file1, 0, null)
        ll4.push(LinkedListEntry(file2, 1))

        val pulled1 = ll.pull()
        assertNotNull(pulled1)
        assertEquals(file1, pulled1?.file)
        assertEquals(0, pulled1?.level)

        val pulled2 = ll.pull()
        assertNotNull(pulled2)
        assertEquals(file2, pulled2?.file)
        assertEquals(1, pulled2?.level)

        val pulled3 = ll.pull()
        assertNotNull(pulled3)
        assertEquals(file3, pulled3?.file)
        assertEquals(2, pulled3?.level)

        assertNull(ll.pull())

        // Coverage for setter of first and last
        val ll3 = LinkedList()
        ll3.first = Entry(file1, 0, null)
        ll3.last = Entry(file2, 1, null)
        assertEquals(file1, ll3.first?.data)
        assertEquals(file2, ll3.last?.data)
    }

    @Test
    fun testEntryDataClass() {
        val e1 = Entry(File("a"), 1, null)
        val e2 = Entry(File("a"), 1, null)
        assertEquals(e1, e2)
        assertEquals(e1.hashCode(), e2.hashCode())
        assertTrue(e1.toString().startsWith("Entry(data="))
        e1.next = Entry(File("b"), 2, null)
    }

    @Test
    fun testLinkedListEntryDataClass() {
        val le1 = LinkedListEntry(File("a"), 1)
        val le2 = LinkedListEntry(File("a"), 1)
        assertEquals(le1, le2)
        assertEquals(le1.hashCode(), le2.hashCode())
        assertTrue(le1.toString().startsWith("LinkedListEntry(file="))
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

    @Test
    fun testGoAndSymlink() {
        val tmpDir = Files.createTempDirectory("testHtml4Tree").toFile()
        tmpDir.deleteOnExit()

        val subDir = File(tmpDir, "subdir")
        subDir.mkdir()

        val fileA = File(subDir, "fileA.txt")
        fileA.writeText("hello")

        val targetDir = Files.createTempDirectory("testHtml4TreeTarget").toFile()
        targetDir.deleteOnExit()

        val symlink = File(tmpDir, "symlinkdir")
        Files.createSymbolicLink(symlink.toPath(), targetDir.toPath())

        val ignoreFile = File(subDir, ".html4ignore")
        ignoreFile.writeText(".*\\.txt")

        go(tmpDir.absolutePath, 1)

        val indexHtml = File(tmpDir, "index.html")
        assertTrue(indexHtml.exists())
        val indexHtmlSub = File(subDir, "index.html")
        assertTrue(indexHtmlSub.exists())

        // Symlink target should NOT contain index.html due to the fix
        val indexHtmlTarget = File(targetDir, "index.html")
        assertTrue(!indexHtmlTarget.exists())

        // Excluded file test (should not be in index)
        val indexContent = indexHtmlSub.readText()
        assertTrue(!indexContent.contains("fileA.txt"))

        // Go with maxLevel 0
        val maxLevel0Dir = Files.createTempDirectory("testMaxLevel0").toFile()
        val mSubdir = File(maxLevel0Dir, "subdir")
        mSubdir.mkdir()
        go(maxLevel0Dir.absolutePath, 0)
        assertTrue(File(maxLevel0Dir, "index.html").exists())
        // Since subdir is empty except for what could be created, we expect no index.html inside subdir
        assertTrue(!File(mSubdir, "index.html").exists())
    }

    @Test
    fun testHtml4TreeClikt() {
        val tmpDir = Files.createTempDirectory("testClikt").toFile()
        tmpDir.deleteOnExit()

        val app = Html4tree()
        app.parse(arrayOf(tmpDir.absolutePath))
        assertTrue(File(tmpDir, "index.html").exists())
    }

    @Test
    fun testMainFunction() {
        val tmpDir = Files.createTempDirectory("testMain").toFile()
        tmpDir.deleteOnExit()

        main(arrayOf(tmpDir.absolutePath))
        assertTrue(File(tmpDir, "index.html").exists())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGoInvalidDir() {
        go("/path/to/non/existent/dir/123456", -1)
    }

    @Test(expected = UsageError::class)
    fun testMainNoArgs() {
        Html4tree().parse(arrayOf())
    }

    @Test(expected = PrintHelpMessage::class)
    fun testMainHelp() {
        Html4tree().parse(arrayOf("-h"))
    }

    @Test
    fun testProcessIgnoreFileNoExclusion() {
        val tmpDir = Files.createTempDirectory("testIgnore").toFile()
        val res = process_ignore_file(tmpDir)
        assertEquals(listOf("index.html"), res)
    }
}
