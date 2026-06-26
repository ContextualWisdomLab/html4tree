package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.io.File

class UtilTest {

    @Test
    fun testLinkedList() {
        val ll = LinkedList()
        val file1 = File("file1")
        val file2 = File("file2")
        val file3 = File("file3")

        assertNull(ll.pull())

        ll.push(LinkedListEntry(file1, 0))
        ll.push(LinkedListEntry(file2, 1))
        ll.push(LinkedListEntry(file3, 2))

        val entry1 = ll.pull()
        assertNotNull(entry1)
        assertEquals(file1, entry1?.file)
        assertEquals(0, entry1?.level)

        val entry2 = ll.pull()
        assertNotNull(entry2)
        assertEquals(file2, entry2?.file)
        assertEquals(1, entry2?.level)

        val entry3 = ll.pull()
        assertEquals(file3, entry3?.file)
        assertEquals(2, entry3?.level)

        assertNull(ll.pull())
    }

    @Test
    fun testLinkedListPushExisting() {
        val ll = LinkedList()
        val file1 = File("file1")
        val file2 = File("file2")

        ll.push(LinkedListEntry(file1, 0))
        ll.push(LinkedListEntry(file2, 1)) // Covers the 'else' branch in push

        val firstEntry = ll.pull()
        assertNotNull(firstEntry)
        assertEquals(file1, firstEntry?.file)
    }

    @Test
    fun testLinkedListGettersSetters() {
        val ll = LinkedList()
        ll.first = Entry(File("test"), 0, null)
        ll.last = Entry(File("test"), 0, null)
        assertNotNull(ll.first)
        assertNotNull(ll.last)
    }
    @Test
    fun testLinkedListPushFirstNull() {
        val ll = LinkedList()
        ll.last = Entry(File("test"), 0, null)
        ll.first = null
        ll.push(LinkedListEntry(File("test"), 0)) // This will trigger the null branch of first?.next
    }
}
