package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.io.File

class UtilTest {

    @Test
    fun testLinkedListPushAndPull() {
        val list = LinkedList()
        val file1 = File("test1")
        val file2 = File("test2")

        // First push
        list.push(LinkedListEntry(file1, 0))
        assertNotNull(list.first)
        assertNotNull(list.last)

        // Second push - sets first?.next but not last, and then first = first?.next
        list.push(LinkedListEntry(file2, 1))

        // First pull reads `last` (test1) and sets `last` to `last.next`
        val pulled1 = list.pull()
        assertNotNull(pulled1)
        assertEquals(file1, pulled1?.file)

        // At this point last should point to the second entry
        val pulled2 = list.pull()
        assertNotNull(pulled2)
        assertEquals(file2, pulled2?.file)

        val pulled3 = list.pull()
        assertNull(pulled3)
    }

    @Test
    fun testLinkedListPullEmpty() {
        val list = LinkedList()
        assertNull(list.pull())
    }

    @Test
    fun testLinkedListSetters() {
        val list = LinkedList()
        val f = File("test")
        val entry = Entry(f, 0, null)
        list.first = entry
        list.last = entry
        assertEquals(entry, list.first)
        assertEquals(entry, list.last)
    }

    @Test
    fun testDataClasses() {
        val f = File("test")
        val entry1 = Entry(f, 0, null)
        val entry2 = Entry(f, 0, null)
        assertEquals(entry1, entry2)
        assertEquals(entry1.hashCode(), entry2.hashCode())
        assertTrue(entry1.toString().contains("Entry"))

        val lle1 = LinkedListEntry(f, 0)
        val lle2 = LinkedListEntry(f, 0)
        assertEquals(lle1, lle2)
        assertEquals(lle1.hashCode(), lle2.hashCode())
        assertTrue(lle1.toString().contains("LinkedListEntry"))

        // cover copy methods
        val entry3 = entry1.copy()
        assertEquals(entry1, entry3)

        val lle3 = lle1.copy()
        assertEquals(lle1, lle3)

        // cover individual getters
        assertEquals(f, entry1.data)
        assertEquals(0, entry1.level)
        assertNull(entry1.next)

        entry1.next = entry2
        assertEquals(entry2, entry1.next)

        assertEquals(f, lle1.file)
        assertEquals(0, lle1.level)
    }
}
