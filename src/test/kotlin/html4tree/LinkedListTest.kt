package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.io.File

class LinkedListTest {
    @Test
    fun testLinkedList() {
        val list = LinkedList()
        val entry1 = LinkedListEntry(File("dir1"), 0)
        val entry2 = LinkedListEntry(File("dir2"), 1)

        assertNull(list.pull())

        list.push(entry1)
        list.push(entry2)

        val pulled1 = list.pull()
        assertNotNull(pulled1)
        assertEquals("dir1", pulled1?.file?.name)
        assertEquals(0, pulled1?.level)

        val pulled2 = list.pull()
        assertNotNull(pulled2)
        assertEquals("dir2", pulled2?.file?.name)
        assertEquals(1, pulled2?.level)

        assertNull(list.pull())
    }
}
