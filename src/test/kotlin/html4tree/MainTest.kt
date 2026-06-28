package html4tree

import org.junit.Test
import java.io.File
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNull
import kotlin.test.assertNotNull

class MainTest {

    @Test
    fun testUtilLinkedList() {
        val ll = LinkedList()
        assertNull(ll.pull())

        val f1 = File("f1")
        val f2 = File("f2")
        ll.push(LinkedListEntry(f1, 0))
        ll.push(LinkedListEntry(f2, 1))

        val p1 = ll.pull()
        assertNotNull(p1)
        assertEquals(f1, p1!!.file)
        assertEquals(0, p1!!.level)

        val p2 = ll.pull()
        assertNotNull(p2)
        assertEquals(f2, p2!!.file)
        assertEquals(1, p2!!.level)

        assertNull(ll.pull())
    }

    @Test
    fun testEscapeHtml() {
        assertEquals("&amp;&lt;&gt;&quot;&#x27;", "&<>\"'".escapeHtml())
    }

    @Test
    fun testUrlEncodePath() {
        assertEquals("a%20b%2Bc", "a b+c".urlEncodePath())
    }

    @Test
    fun testHelp() {
        val oldOut = System.out
        val baos = ByteArrayOutputStream()
        val ps = PrintStream(baos, false, "UTF-8")
        System.setOut(ps)
        try {
            help()
        } finally {
            System.setOut(oldOut)
            ps.close()
        }
        assertTrue(baos.toString("UTF-8").contains("ERROR: help has not been written yet!"))
    }

    @Test
    fun testProcessDir() {
        val tempDir = createTempDir("testdir")
        val subDir = File(tempDir, "subdir")
        subDir.mkdir()
        val file1 = File(tempDir, "file1.txt")
        file1.writeText("hello")
        val file2 = File(tempDir, "file2.txt")
        file2.writeText("world")

        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText("file1\\.txt\n")

        process_dir(tempDir)

        val indexHtml = File(tempDir, "index.html")
        assertTrue(indexHtml.exists())
        val content = indexHtml.readText()
        assertTrue(content.contains("subdir"))
        assertTrue(content.contains("file2.txt"))
        assertTrue(!content.contains("file1.txt"))

        tempDir.deleteRecursively()
    }

    @Test
    fun testGo() {
        val tempDir = createTempDir("testdir2")
        val sub1 = File(tempDir, "sub1")
        sub1.mkdir()
        File(sub1, "f.txt").writeText("a")

        go(tempDir.absolutePath, 1)
        assertTrue(File(tempDir, "index.html").exists())
        assertTrue(File(sub1, "index.html").exists())

        tempDir.deleteRecursively()
    }

    @Test
    fun testGoLimitLevel() {
        val tempDir = createTempDir("testdir_level")
        val sub1 = File(tempDir, "sub1")
        sub1.mkdir()
        val sub2 = File(sub1, "sub2")
        sub2.mkdir()

        go(tempDir.absolutePath, 0)
        assertTrue(File(tempDir, "index.html").exists())
        assertTrue(!File(sub1, "index.html").exists())
        assertTrue(!File(sub2, "index.html").exists())

        tempDir.deleteRecursively()
    }

    @Test
    fun testMain() {
        val tempDir = createTempDir("testdir3")
        main(arrayOf("--max-level", "0", tempDir.absolutePath))
        assertTrue(File(tempDir, "index.html").exists())
        tempDir.deleteRecursively()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGoException() {
        go("non_existent_directory_for_test", -1)
    }
}
