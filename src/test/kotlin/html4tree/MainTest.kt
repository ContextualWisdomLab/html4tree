package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.nio.file.Files

class MainTest {

    @Test
    fun testEscapeHtml() {
        assertEquals("&amp;", "&".escapeHtml())
        assertEquals("&lt;", "<".escapeHtml())
        assertEquals("&gt;", ">".escapeHtml())
        assertEquals("&quot;", "\"".escapeHtml())
        assertEquals("&#x27;", "'".escapeHtml())
        assertEquals("normal", "normal".escapeHtml())
    }

    @Test
    fun testUrlEncodePath() {
        assertEquals("a%20b", "a b".urlEncodePath())
        assertEquals("a%2Bb", "a+b".urlEncodePath())
        assertEquals("a%2Fb", "a/b".urlEncodePath())
    }

    @Test
    fun testLinkedList() {
        val ll = LinkedList()
        assertNull(ll.pull()) // Empty pull

        val f1 = File("f1")
        val e1 = LinkedListEntry(f1, 1)
        ll.push(e1) // Push to empty list (last == null)

        val f2 = File("f2")
        val e2 = LinkedListEntry(f2, 2)
        ll.push(e2) // Push to non-empty list (first?.next is called)

        val p1 = ll.pull() // Pull non-empty, sets last = l.next
        assertEquals(f1, p1?.file)
        assertEquals(1, p1?.level)

        val p2 = ll.pull()
        assertEquals(f2, p2?.file)
        assertEquals(2, p2?.level)

        assertNull(ll.pull()) // Pull empty list
    }

    @Test
    fun testLinkedListFirstNull() {
        // Test edge case where last != null but first == null
        val ll = LinkedList()
        ll.last = Entry(File("a"), 1, null)
        ll.first = null
        val e1 = LinkedListEntry(File("b"), 2)
        ll.push(e1) // this will exercise the first?.next branches evaluating to null

        assertNull(ll.first) // since first was null, first?.next doesn't do anything and first remains null

        val p = ll.pull()
        assertEquals(File("a"), p?.file)
        assertNull(ll.pull())
    }

    @Test
    fun testLinkedListPullNullLast() {
        val ll = LinkedList()
        ll.last = null
        assertNull(ll.pull())
    }

    @Test
    fun testLinkedListSettersAndGetters() {
        val ll = LinkedList()
        ll.first = Entry(File("a"), 1, null)
        ll.last = ll.first
        assertEquals("a", ll.first?.data?.name)
        assertEquals("a", ll.last?.data?.name)
    }

    @Test
    fun testEntryAndLinkedListEntry() {
        val f1 = File("f1")
        val lle = LinkedListEntry(f1, 0)
        assertEquals(f1, lle.file)
        assertEquals(0, lle.level)
        val copyLle = lle.copy(level = 1)
        assertEquals(1, copyLle.level)
        val copyLle2 = lle.copy(file = File("f2"))
        assertEquals("f2", copyLle2.file.name)
        assertEquals("LinkedListEntry(file=f1, level=0)", lle.toString())
        assertTrue(lle == LinkedListEntry(f1, 0))
        assertEquals(LinkedListEntry(f1, 0).hashCode(), lle.hashCode())
        val comp1 = LinkedListEntry(f1,0).component1()
        val comp2 = LinkedListEntry(f1,0).component2()
        assertEquals(f1, comp1)
        assertEquals(0, comp2)

        val entry = Entry(f1, 0, null)
        assertEquals(f1, entry.data)
        assertEquals(0, entry.level)
        assertNull(entry.next)
        val copyEntry = entry.copy(level = 1)
        assertEquals(1, copyEntry.level)
        val copyEntry2 = entry.copy(data = File("f2"))
        assertEquals("f2", copyEntry2.data.name)
        val copyEntry3 = entry.copy(next = Entry(f1, 1, null))
        assertNotNull(copyEntry3.next)
        assertEquals("Entry(data=f1, level=0, next=null)", entry.toString())
        assertTrue(entry == Entry(f1, 0, null))
        assertEquals(Entry(f1, 0, null).hashCode(), entry.hashCode())

        entry.next = Entry(f1, 1, null)
        assertNotNull(entry.next)

        val ecomp1 = entry.component1()
        val ecomp2 = entry.component2()
        val ecomp3 = entry.component3()
        assertEquals(f1, ecomp1)
        assertEquals(0, ecomp2)
        assertNotNull(ecomp3)
    }

    @Test
    fun testHelp() {
        val outContent = java.io.ByteArrayOutputStream()
        System.setOut(java.io.PrintStream(outContent))
        help()
        assertEquals("ERROR: help has not been written yet!\n", outContent.toString())
        System.setOut(System.out)
    }

    @Test
    fun testProcessIgnoreFile() {
        val tempDir = Files.createTempDirectory("test").toFile()
        tempDir.deleteOnExit()

        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText(".*\\.txt\nfoo\\.bar")

        val file1 = File(tempDir, "a.txt")
        file1.createNewFile()

        val file2 = File(tempDir, "b.pdf")
        file2.createNewFile()

        val file3 = File(tempDir, "foo.bar")
        file3.createNewFile()

        val excluded = process_ignore_file(tempDir)
        assertTrue(excluded.contains("a.txt"))
        assertFalse(excluded.contains("b.pdf"))
        assertTrue(excluded.contains("foo.bar"))
        assertTrue(excluded.contains("index.html"))
    }

    @Test
    fun testProcessIgnoreFileMissing() {
        val tempDir = Files.createTempDirectory("test").toFile()
        tempDir.deleteOnExit()

        val excluded = process_ignore_file(tempDir)
        assertTrue(excluded.contains("index.html"))
        assertEquals(1, excluded.size)
    }

    @Test
    fun testProcessIgnoreFileWithIndexHtml() {
        val tempDir = Files.createTempDirectory("test").toFile()
        tempDir.deleteOnExit()

        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText("index\\.html")

        File(tempDir, "index.html").createNewFile()

        val excluded = process_ignore_file(tempDir)
        assertTrue(excluded.contains("index.html"))
    }

    @Test
    fun testProcessDir() {
        val tempDir = Files.createTempDirectory("test_process_dir").toFile()
        tempDir.deleteOnExit()

        val subDir1 = File(tempDir, "sub 1")
        subDir1.mkdir()
        subDir1.deleteOnExit()

        val subDir2 = File(tempDir, "sub2")
        subDir2.mkdir()
        subDir2.deleteOnExit()

        val file1 = File(tempDir, "file1.txt")
        file1.createNewFile()
        file1.deleteOnExit()

        val fileExcluded = File(tempDir, "excluded.txt")
        fileExcluded.createNewFile()
        fileExcluded.deleteOnExit()

        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText("excluded\\.txt")
        ignoreFile.deleteOnExit()

        process_dir(tempDir)

        val indexHtml = File(tempDir, "index.html")
        assertTrue(indexHtml.exists())

        val content = indexHtml.readText()
        assertTrue(content.contains("<meta charset=\"utf-8\">"))
        assertTrue(content.contains("Content-Security-Policy"))
        assertTrue(content.contains("sub%201"))
        assertTrue(content.contains("sub2"))
        assertTrue(content.contains("file1.txt"))
        assertFalse(content.contains("excluded.txt"))
        assertTrue(content.contains("&#128193;")) // directory icon
        assertTrue(content.contains("&rtrif;")) // file icon

        indexHtml.deleteOnExit()
    }

    @Test
    fun testProcessDirSelfDirExclusion() {
        val tempDir = Files.createTempDirectory("test_process_dir_self").toFile()
        tempDir.deleteOnExit()

        // Ensure index.html is generated which loops through subdirs
        val subDir1 = File(tempDir, "sub 1")
        subDir1.mkdir()
        subDir1.deleteOnExit()

        process_dir(tempDir)
        val indexHtml = File(tempDir, "index.html")
        assertTrue(indexHtml.exists())
    }

    @Test
    fun testGo() {
        val tempDir = Files.createTempDirectory("test_go").toFile()
        tempDir.deleteOnExit()

        val subDir1 = File(tempDir, "sub1")
        subDir1.mkdir()
        subDir1.deleteOnExit()

        val subDir2 = File(subDir1, "sub2")
        subDir2.mkdir()
        subDir2.deleteOnExit()

        val f1 = File(tempDir, "file1.txt")
        f1.createNewFile()
        f1.deleteOnExit()

        go(tempDir.absolutePath, 1)

        val indexTop = File(tempDir, "index.html")
        assertTrue(indexTop.exists())
        indexTop.deleteOnExit()

        val indexSub1 = File(subDir1, "index.html")
        assertTrue(indexSub1.exists())
        indexSub1.deleteOnExit()

        val indexSub2 = File(subDir2, "index.html")
        assertFalse(indexSub2.exists())
    }

    @Test
    fun testGoNullCurrentLevel() {
        val tempDir = Files.createTempDirectory("test_go").toFile()
        tempDir.deleteOnExit()

        // to cover condition where lle != null && !lle.file.isDirectory()
        val file1 = File(tempDir, "file1.txt")
        file1.createNewFile()
    }

    @Test
    fun testGoMaxLevelMinus1() {
        val tempDir = Files.createTempDirectory("test_go_unlimited").toFile()
        tempDir.deleteOnExit()

        val subDir1 = File(tempDir, "sub1")
        subDir1.mkdir()
        subDir1.deleteOnExit()

        val subDir2 = File(subDir1, "sub2")
        subDir2.mkdir()
        subDir2.deleteOnExit()

        go(tempDir.absolutePath, -1)

        val indexSub2 = File(subDir2, "index.html")
        assertTrue(indexSub2.exists())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGoInvalidDir() {
        go("/invalid/path/that/does/not/exist", 1)
    }

    @Test
    fun testHtml4treeApp() {
        val tempDir = Files.createTempDirectory("test_app").toFile()
        tempDir.deleteOnExit()

        val subDir1 = File(tempDir, "sub1")
        subDir1.mkdir()
        subDir1.deleteOnExit()

        main(arrayOf(tempDir.absolutePath))
        assertTrue(File(tempDir, "index.html").exists())
        assertTrue(File(subDir1, "index.html").exists())
    }

    @Test
    fun testHtml4treeAppWithOption() {
        val tempDir = Files.createTempDirectory("test_app").toFile()
        tempDir.deleteOnExit()

        val subDir1 = File(tempDir, "sub1")
        subDir1.mkdir()
        subDir1.deleteOnExit()

        val subDir2 = File(subDir1, "sub2")
        subDir2.mkdir()
        subDir2.deleteOnExit()

        main(arrayOf("--max-level", "0", tempDir.absolutePath))
        assertTrue(File(tempDir, "index.html").exists())
        assertFalse(File(subDir1, "index.html").exists())
    }
}
