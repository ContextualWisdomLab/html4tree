package html4tree

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull
import java.io.File

class UtilTest {

    @Test
    fun testEntry() {
        val f1 = File("foo")
        val e1 = Entry(f1, 1, null)
        val e2 = Entry(File("bar"), 2, e1)

        assertEquals(f1, e1.data)
        assertEquals(1, e1.level)
        assertNull(e1.next)

        assertEquals(e1, e2.next)
    }

    @Test
    fun testLinkedListEntry() {
        val f1 = File("foo")
        val lle = LinkedListEntry(f1, 5)
        assertEquals(f1, lle.file)
        assertEquals(5, lle.level)
    }

    @Test
    fun testLinkedListPushPullBuggyBehavior() {
        val ll = LinkedList()

        val f1 = File("file1")
        val f2 = File("file2")
        val f3 = File("file3")

        val lle1 = LinkedListEntry(f1, 1)
        val lle2 = LinkedListEntry(f2, 2)
        val lle3 = LinkedListEntry(f3, 3)

        ll.push(lle1)
        assertNotNull(ll.first)
        assertNotNull(ll.last)
        assertEquals(f1, ll.first?.data)
        assertEquals(f1, ll.last?.data)
        assertEquals(1, ll.first?.level)
        assertNull(ll.first?.next)

        ll.push(lle2)
        assertNotNull(ll.last)
        assertEquals(f1, ll.last?.data)
        assertEquals(f2, ll.first?.data)
        assertEquals(2, ll.first?.level)
        assertNull(ll.first?.next)

        val pulled1 = ll.pull()
        assertNotNull(pulled1)
        assertEquals(f1, pulled1.file)
        assertEquals(1, pulled1.level)

        val pulled2 = ll.pull()
        assertNotNull(pulled2)
        assertEquals(f2, pulled2.file)

        val pulled3 = ll.pull()
        assertNull(pulled3)

        ll.push(lle3)
        assertEquals(f3, ll.first?.data)
        assertEquals(f3, ll.last?.data)
    }

    @Test
    fun testLinkedListBranches() {
        val ll = LinkedList()
        val lle1 = LinkedListEntry(File("1"), 1)
        val lle2 = LinkedListEntry(File("2"), 2)

        assertNull(ll.pull())

        ll.push(lle1)
        ll.push(lle2)

        ll.pull()
        ll.pull()
        ll.pull()

        val ll3 = LinkedList()
        ll3.last = Entry(File("dummy"), 0, null)
        ll3.first = null
        ll3.push(lle1)

        val ll4 = LinkedList()
        ll4.last = Entry(File("dummy"), 0, null)
        ll4.first = null
        ll4.pull()
    }
}
