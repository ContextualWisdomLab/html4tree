package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.io.File

class LinkedListTest {
    @Test
    fun testGettersAndSetters() {
        val ll = LinkedList()
        val e1 = Entry(File("test"), 0, null)
        ll.first = e1
        assertEquals(e1, ll.first)

        val e2 = Entry(File("test2"), 1, null)
        ll.last = e2
        assertEquals(e2, ll.last)
    }
}
