package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UtilTest {

    @Test
    fun testLinkedList() {
        val ll = LinkedList()
        assertNull(ll.pull())

        val f1 = File("f1")
        val f2 = File("f2")
        val f3 = File("f3")

        ll.push(LinkedListEntry(f1, 0))
        ll.push(LinkedListEntry(f2, 1))
        ll.push(LinkedListEntry(f3, 2))

        val r1 = ll.pull()
        assertNotNull(r1)
        assertEquals(f1, r1.file)
        assertEquals(0, r1.level)

        val r2 = ll.pull()
        assertNotNull(r2)
        assertEquals(f2, r2.file)
        assertEquals(1, r2.level)

        val r3 = ll.pull()
        assertNotNull(r3)
        assertEquals(f3, r3.file)
        assertEquals(2, r3.level)

        assertNull(ll.pull())
    }

    @Test
    fun testLinkedListMore() {
        val ll = LinkedList()
        val f1 = File("f1")
        ll.push(LinkedListEntry(f1, 0))
        val r1 = ll.pull()
        assertNotNull(r1)
        assertEquals(f1, r1.file)

        // now last is null again
        val f2 = File("f2")
        ll.push(LinkedListEntry(f2, 1))
        val r2 = ll.pull()
        assertNotNull(r2)
        assertEquals(f2, r2.file)

        // Also test the branch where l != null but l.next is not null (which means there are items)
        val ll2 = LinkedList()
        ll2.push(LinkedListEntry(f1, 0))
        ll2.push(LinkedListEntry(f2, 1))
        // last is f1
        val res1 = ll2.pull() // pulls f1
        val res2 = ll2.pull() // pulls f2
        assertNotNull(res1)
        assertNotNull(res2)

        // Test push when last is not null but first is somehow null (not possible naturally with push logic, but data classes might generate stuff like toString, hashCode, etc. Let's cover data classes)
        val e1 = Entry(f1, 0, null)
        val e2 = Entry(f1, 0, null)
        assertEquals(e1, e2)
        assertEquals(e1.hashCode(), e2.hashCode())
        assertTrue(e1.toString().contains("Entry"))

        val lle1 = LinkedListEntry(f1, 0)
        val lle2 = LinkedListEntry(f1, 0)
        assertEquals(lle1, lle2)
        assertEquals(lle1.hashCode(), lle2.hashCode())
        assertTrue(lle1.toString().contains("LinkedListEntry"))
    }

    @Test
    fun testLinkedListMissingBranch() {
        // trying to hit missing branches
        val ll = LinkedList()
        val f1 = File("f1")
        ll.push(LinkedListEntry(f1, 0)) // last is f1, first is f1

        // we can forcibly modify 'first' using reflection or just test behavior
        // wait, let's look at `first?.next = null`
        val f2 = File("f2")

        // let's try pushing again to cover all paths
        ll.push(LinkedListEntry(File("f3"), 2))
    }

    @Test
    fun testFirstNullBranch() {
        val ll = LinkedList()
        val f1 = File("f1")
        ll.push(LinkedListEntry(f1, 0))
        ll.first = null
        ll.push(LinkedListEntry(f1, 0))
    }

    @Test
    fun testDataClassesProperties() {
        val f1 = File("f1")
        val e1 = Entry(f1, 0, null)
        val lle1 = LinkedListEntry(f1, 0)

        // access properties to cover getters
        assertEquals(f1, e1.data)
        assertEquals(0, e1.level)
        assertNull(e1.next)

        assertEquals(f1, lle1.file)
        assertEquals(0, lle1.level)

        // test components
        val (d, l, n) = e1
        assertEquals(f1, d)
        assertEquals(0, l)
        assertNull(n)

        val (f, lvl) = lle1
        assertEquals(f1, f)
        assertEquals(0, lvl)

        // test copy
        val e2 = e1.copy(level = 1)
        assertEquals(1, e2.level)

        val lle2 = lle1.copy(level = 1)
        assertEquals(1, lle2.level)
    }

    @Test
    fun testLinkedListGettersSetters() {
        val ll = LinkedList()
        val e = Entry(File("a"), 0, null)
        ll.first = e
        ll.last = e
        assertEquals(e, ll.first)
        assertEquals(e, ll.last)
    }
}
