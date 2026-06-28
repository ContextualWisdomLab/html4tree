package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.io.File

class UtilTest {
    @Test
    fun testEntry() {
        val file = File("test")
        val entry = Entry(file, 0, null)
        assertEquals(file, entry.data)
        assertEquals(0, entry.level)
        assertNull(entry.next)
    }

    @Test
    fun testLinkedListEntry() {
        val file = File("test")
        val lle = LinkedListEntry(file, 0)
        assertEquals(file, lle.file)
        assertEquals(0, lle.level)
    }

    @Test
    fun testLinkedList() {
        val list = LinkedList()
        assertNull(list.first)
        assertNull(list.last)

        val file1 = File("test1")
        val file2 = File("test2")
        val file3 = File("test3")

        list.push(LinkedListEntry(file1, 0))
        assertNotNull(list.first)
        assertNotNull(list.last)
        assertEquals(list.first, list.last)

        list.push(LinkedListEntry(file2, 1))
        assertEquals(file2, list.first?.data)
        assertEquals(file1, list.last?.data)

        list.push(LinkedListEntry(file3, 2))
        assertEquals(file3, list.first?.data)

        val pulled1 = list.pull()
        assertNotNull(pulled1)
        assertEquals(file1, pulled1?.file)
        assertEquals(0, pulled1?.level)

        val pulled2 = list.pull()
        assertEquals(file2, pulled2?.file)
        assertEquals(1, pulled2?.level)

        val pulled3 = list.pull()
        assertEquals(file3, pulled3?.file)
        assertEquals(2, pulled3?.level)

        assertNull(list.pull())

        list.push(LinkedListEntry(file1, 0))
        val pulledAfterReuse = list.pull()
        assertEquals(file1, pulledAfterReuse?.file)
        assertEquals(0, pulledAfterReuse?.level)
        assertNull(list.pull())
    }
}
