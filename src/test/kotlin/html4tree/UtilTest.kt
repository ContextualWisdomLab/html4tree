package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.io.File

class UtilTest {
    @Test
    fun testLinkedList() {
        val ll = LinkedList()

        val f1 = File("f1")
        val f2 = File("f2")
        val f3 = File("f3")

        ll.push(LinkedListEntry(f1, 1))
        ll.push(LinkedListEntry(f2, 2))
        ll.push(LinkedListEntry(f3, 3))

        val p1 = ll.pull()
        assertNotNull(p1)
        assertEquals(f1, p1?.file)
        assertEquals(1, p1?.level)

        val p2 = ll.pull()
        assertNotNull(p2)
        assertEquals(f2, p2?.file)
        assertEquals(2, p2?.level)

        val p3 = ll.pull()
        assertNotNull(p3)
        assertEquals(f3, p3?.file)
        assertEquals(3, p3?.level)

        val p4 = ll.pull()
        assertNull(p4)
    }
}
