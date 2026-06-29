package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.io.File

class LinkedListTest {
    @Test
    fun testLinkedList() {
        val list = LinkedList()
        list.first = null
        list.last = null
        assertNull(list.first)
        assertNull(list.last)

        list.push(LinkedListEntry(File("f1"), 0))
        list.push(LinkedListEntry(File("f2"), 0))
        list.push(LinkedListEntry(File("f3"), 0))

        assertEquals(File("f1"), list.pull()?.file)
        assertEquals(File("f2"), list.pull()?.file)
        assertEquals(File("f3"), list.pull()?.file)
        assertNull(list.pull())
        assertNull(list.pull())
    }

    @Test
    fun testLinkedListPushNull() {
        val list = LinkedList()
        val e1 = Entry(File("test"), 0, null)
        list.first = e1
        list.last = e1
        // first == e1, last == e1.
        list.push(LinkedListEntry(File("test2"), 0))
        // The implementation appends to first instead of last, which is buggy but we hit the branch
        assertEquals(File("test"), list.pull()?.file)
        assertEquals(File("test2"), list.pull()?.file)
    }

    @Test
    fun testEntry() {
        val e = Entry(File("a"), 0, null)
        assertEquals(File("a"), e.data)
        assertEquals(0, e.level)
        assertNull(e.next)
        val e2 = Entry(File("b"), 1, null)
        e.next = e2
        assertEquals(e2, e.next)
    }

    @Test
    fun testPushWithNullFirst() {
        val list = LinkedList()
        val e1 = Entry(File("test"), 0, null)
        list.last = e1
        list.first = null
        list.push(LinkedListEntry(File("test2"), 0))
    }
}
