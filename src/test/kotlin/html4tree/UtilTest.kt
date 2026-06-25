package html4tree

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import java.io.File

class UtilTest {
    @Test
    fun testLinkedList() {
        val list = LinkedList()
        val entry1 = LinkedListEntry(File("dir1"), 0)
        val entry2 = LinkedListEntry(File("dir2"), 1)

        assertNull(list.pull())

        list.push(entry1)
        list.push(entry2)

        val pulled1 = list.pull()
        assertEquals(entry1, pulled1)

        val pulled2 = list.pull()
        assertEquals(entry2, pulled2)

        assertNull(list.pull())
    }

    @Test
    fun testLinkedListMultiplePushes() {
        val list = LinkedList()
        val entry1 = LinkedListEntry(File("dir1"), 0)
        val entry2 = LinkedListEntry(File("dir2"), 1)
        val entry3 = LinkedListEntry(File("dir3"), 2)

        list.push(entry1)
        list.push(entry2)
        list.push(entry3)

        assertEquals(entry1, list.pull())
        assertEquals(entry2, list.pull())
        assertEquals(entry3, list.pull())
        assertNull(list.pull())
    }

    @Test
    fun testLinkedListProperties() {
        val list = LinkedList()
        assertNull(list.first)
        assertNull(list.last)

        val entry = Entry(File("dir"), 0, null)
        list.first = entry
        list.last = entry

        assertEquals(entry, list.first)
        assertEquals(entry, list.last)
    }

    @Test
    fun testPushWithNullFirst() {
        val list = LinkedList()
        list.last = Entry(File("dir1"), 0, null)
        list.first = null
        list.push(LinkedListEntry(File("dir2"), 1))
        assertEquals("dir2", list.first?.data?.name)
        assertEquals("dir2", list.last?.data?.name)
    }
}