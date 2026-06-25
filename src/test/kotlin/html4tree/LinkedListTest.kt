package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNull
import kotlin.test.assertNotNull

class LinkedListTest {

    @Test
    fun testPushAndPullEmpty() {
        val ll = LinkedList()
        assertNull(ll.pull())
    }

    @Test
    fun testPushAndPullSingle() {
        val ll = LinkedList()
        val f1 = File("f1")
        ll.push(LinkedListEntry(f1, 0))
        val pulled = ll.pull()
        assertNotNull(pulled)
        assertEquals(f1, pulled!!.file)
        assertNull(ll.pull())
    }

    @Test
    fun testPushAndPullMultiple() {
        val ll = LinkedList()
        val f1 = File("f1")
        val f2 = File("f2")
        val f3 = File("f3")
        ll.push(LinkedListEntry(f1, 0))
        ll.push(LinkedListEntry(f2, 1))
        ll.push(LinkedListEntry(f3, 2))

        val p1 = ll.pull()
        assertNotNull(p1)
        assertEquals(f1, p1!!.file)

        val p2 = ll.pull()
        assertNotNull(p2)
        assertEquals(f2, p2!!.file)

        val p3 = ll.pull()
        assertNotNull(p3)
        assertEquals(f3, p3!!.file)

        assertNull(ll.pull())
    }
}
