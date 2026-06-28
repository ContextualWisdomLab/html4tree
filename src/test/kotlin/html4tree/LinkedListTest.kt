package html4tree

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import java.io.File

class LinkedListTest {

    @Test
    fun testLinkedList() {
        val ll = LinkedList()
        assertNull(ll.pull())

        val f1 = File("file1")
        val f2 = File("file2")
        val f3 = File("file3")

        ll.push(LinkedListEntry(f1, 0))
        ll.push(LinkedListEntry(f2, 1))
        ll.push(LinkedListEntry(f3, 2))

        val e1 = ll.pull()
        assertEquals(f1, e1?.file)
        assertEquals(0, e1?.level)

        val e2 = ll.pull()
        assertEquals(f2, e2?.file)
        assertEquals(1, e2?.level)

        val e3 = ll.pull()
        assertEquals(f3, e3?.file)
        assertEquals(2, e3?.level)

        assertNull(ll.pull())
    }

    @Test
    fun testLinkedListSettersGetters() {
        val ll = LinkedList()
        val e = Entry(File("a"), 0, null)
        ll.first = e
        ll.last = e
        assertEquals(e, ll.first)
        assertEquals(e, ll.last)
    }

    @Test
    fun testPushToNonEmpty() {
        val ll = LinkedList()
        val lle1 = LinkedListEntry(File("1"), 1)
        val lle2 = LinkedListEntry(File("2"), 2)
        ll.push(lle1)
        ll.push(lle2)

        // This exercises the else branch in push
        assertEquals("2", ll.first?.data?.name)
        assertEquals("1", ll.last?.data?.name)
    }

    @Test
    fun testPushWithNullFirst() {
        val ll = LinkedList()
        val lle1 = LinkedListEntry(File("1"), 1)
        val lle2 = LinkedListEntry(File("2"), 2)
        ll.push(lle1)
        ll.first = null // artificially make first null
        ll.push(lle2) // will trigger first?.next safe calls returning null
    }
}
