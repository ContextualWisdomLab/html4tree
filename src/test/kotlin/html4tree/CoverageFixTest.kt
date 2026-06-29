package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.io.File

class CoverageFixTest {
    @Test
    fun testLinkedListFull() {
        val list = LinkedList()
        val e1 = LinkedListEntry(File("a"), 0)
        val e2 = LinkedListEntry(File("b"), 1)

        list.push(e1)
        assertNotNull(list.first)
        assertNotNull(list.last)

        list.push(e2)
        assertNotNull(list.first)
        assertNotNull(list.last)
        assertEquals("b", list.first?.data?.name)
        assertEquals("a", list.last?.data?.name)

        val pulled1 = list.pull()
        assertNotNull(pulled1)
        assertEquals("a", pulled1?.file?.name)

        val pulled2 = list.pull()
        assertNotNull(pulled2)
        assertEquals("b", pulled2?.file?.name)
    }

    @Test
    fun testGoMaxLevel() {
        val tempDir = createTempDir("goMaxTestDir")
        try {
            val childDir1 = File(tempDir, "child1")
            childDir1.mkdirs()

            go(tempDir.absolutePath, -1)
            assertTrue(File(tempDir, "index.html").exists())
            assertTrue(File(childDir1, "index.html").exists())

        } finally {
            tempDir.deleteRecursively()
        }
    }
}
