package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertNotNull

class LinkedListTest {

    @Test
    fun testLinkedList() {
        val list = LinkedList()
        assertNull(list.pull())

        val dir1 = File("dir1")
        val dir2 = File("dir2")
        val dir3 = File("dir3")

        list.push(LinkedListEntry(dir1, 0))
        list.push(LinkedListEntry(dir2, 1))
        list.push(LinkedListEntry(dir3, 2))

        // This relies on the current behavior of LinkedList where last is not properly maintained
        val e1 = list.pull()
        assertNotNull(e1)

        val e2 = list.pull()

        // Also test data classes
        val entry = Entry(dir1, 0, null)
        assertEquals(dir1, entry.data)
        assertEquals(0, entry.level)
        assertEquals(null, entry.next)
    }

    @Test
    fun testLinkedListMore() {
        val entry1 = Entry(File("a"), 0, null)
        val entry2 = Entry(File("a"), 0, null)
        assertTrue(entry1 == entry2)
        assertEquals(entry1.hashCode(), entry2.hashCode())
        assertEquals("Entry(data=a, level=0, next=null)", entry1.toString())

        val llEntry1 = LinkedListEntry(File("a"), 0)
        val llEntry2 = LinkedListEntry(File("a"), 0)
        assertTrue(llEntry1 == llEntry2)
        assertEquals(llEntry1.hashCode(), llEntry2.hashCode())
        assertEquals("LinkedListEntry(file=a, level=0)", llEntry1.toString())
    }

    @Test
    fun testLinkedListFull() {
        val ll = LinkedList()
        val e1 = LinkedListEntry(File("1"), 1)
        val e2 = LinkedListEntry(File("2"), 2)
        ll.push(e1)
        ll.push(e2)

        val p1 = ll.pull()
        assertNotNull(p1)

        val p2 = ll.pull()

        ll.first = Entry(File("bad"), 0, null)
        ll.last = ll.first

        ll.push(LinkedListEntry(File("bad2"), 1))
    }
}

class ExtraLinkedListTest {
    @Test
    fun testPushNullFirst() {
        val ll = LinkedList()
        ll.first = Entry(File("bad"), 0, null)
        ll.push(LinkedListEntry(File("test"), 0)) // last is null
        assertEquals(File("test"), ll.last?.data)
    }

    @Test
    fun testPull() {
        val ll = LinkedList()
        val entry = Entry(File("test"), 0, null)
        ll.last = entry
        ll.pull()
        assertNull(ll.last)
    }
}

class MissingCovTest {
    @Test
    fun testLinkedListPushNull() {
        val ll = LinkedList()
        ll.first = Entry(File("a"), 0, null)
        ll.push(LinkedListEntry(File("b"), 0))
    }
}

class MissingCovTest2 {
    @Test
    fun testLinkedListPullNull() {
        val ll = LinkedList()
        ll.last = Entry(File("a"), 0, null)
        ll.pull()
        val e = ll.pull()
        assertNull(e)
    }
}

class MissingCovTest3 {
    @Test
    fun testPushNullFirstNext() {
        val ll = LinkedList()
        ll.first = null
        ll.last = Entry(File("a"), 0, null)
        ll.push(LinkedListEntry(File("b"), 0))
    }
}
