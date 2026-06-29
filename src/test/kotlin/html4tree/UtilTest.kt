package html4tree

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import java.io.File

class UtilTest {
    @Test
    fun testLinkedList() {
        val list = LinkedList()
        val entry1 = LinkedListEntry(File("dir1"), 1)
        val entry2 = LinkedListEntry(File("dir2"), 2)

        assertNull(list.pull())

        list.push(entry1)
        list.push(entry2)
        list.push(LinkedListEntry(File("dir3"), 3))

        val pulled1 = list.pull()
        assertEquals(entry1, pulled1)

        val pulled2 = list.pull()
        assertEquals(entry2, pulled2)

        val pulled3 = list.pull()
        assertEquals(LinkedListEntry(File("dir3"), 3), pulled3)

        assertNull(list.pull())
    }

    @Test
    fun testLinkedListGettersSetters() {
        val list = LinkedList()
        assertNull(list.first)
        assertNull(list.last)

        val entry = Entry(File("test"), 1, null)
        list.first = entry
        list.last = entry

        assertEquals(entry, list.first)
        assertEquals(entry, list.last)
    }

    @Test
    fun testDataClasses() {
        val entry = Entry(File("test"), 1, null)
        assertEquals(File("test"), entry.data)
        assertEquals(1, entry.level)
        assertNull(entry.next)

        val entry2 = Entry(File("test2"), 2, entry)
        assertEquals(entry, entry2.next)

        val llEntry = LinkedListEntry(File("test3"), 3)
        assertEquals(File("test3"), llEntry.file)
        assertEquals(3, llEntry.level)

        // Test auto-generated functions (toString, equals, hashCode, copy) to reach 100%
        val eCopy = entry.copy()
        assertEquals(entry, eCopy)
        assertEquals(entry.hashCode(), eCopy.hashCode())
        assertEquals(entry.toString(), eCopy.toString())

        val llCopy = llEntry.copy()
        assertEquals(llEntry, llCopy)
        assertEquals(llEntry.hashCode(), llCopy.hashCode())
        assertEquals(llEntry.toString(), llCopy.toString())
    }
}
