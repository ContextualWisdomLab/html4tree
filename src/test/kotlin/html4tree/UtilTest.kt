package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.io.File

class UtilTest {
    @Test
    fun testLinkedList() {
        val ll = LinkedList()
        assertNull(ll.pull())

        val f1 = File("f1")
        val f2 = File("f2")
        val f3 = File("f3")

        ll.push(LinkedListEntry(f1, 0))
        assertEquals(f1, ll.first?.data)
        assertEquals(f1, ll.last?.data)

        // This will hit the else branch
        ll.push(LinkedListEntry(f2, 1))

        // Let's create a situation where first?.next gets hit if we manipulate it
        // actually `first` is non-null when we reach else because of the if block

        val e1 = ll.pull()
        assertNotNull(e1)
        assertEquals(f1, e1?.file)
        assertEquals(0, e1?.level)

        ll.push(LinkedListEntry(f3, 2))

        val e2 = ll.pull()
        assertNotNull(e2)
        assertEquals(f2, e2?.file)
        assertEquals(1, e2?.level)

        val e3 = ll.pull()
        assertNotNull(e3)
        assertEquals(f3, e3?.file)
        assertEquals(2, e3?.level)

        assertNull(ll.pull())

        ll.first = Entry(f1, 0, null)
        ll.last = Entry(f1, 0, null)
        assertEquals(f1, ll.first?.data)
        assertEquals(f1, ll.last?.data)
    }

    @Test
    fun testLinkedListPushNullFirst() {
        val ll = LinkedList()
        ll.last = Entry(File("f"), 0, null)
        ll.first = null
        ll.push(LinkedListEntry(File("f2"), 1))
    }

    @Test
    fun testEntryDataClass() {
        val e1 = Entry(File("f1"), 1, null)
        val e2 = Entry(File("f1"), 1, null)
        assertEquals(e1, e2)
        assertEquals(e1.hashCode(), e2.hashCode())
        assertTrue(e1.toString().contains("Entry"))
    }

    @Test
    fun testLinkedListEntryDataClass() {
        val e1 = LinkedListEntry(File("f1"), 1)
        val e2 = LinkedListEntry(File("f1"), 1)
        assertEquals(e1, e2)
        assertEquals(e1.hashCode(), e2.hashCode())
        assertTrue(e1.toString().contains("LinkedListEntry"))
    }
}
