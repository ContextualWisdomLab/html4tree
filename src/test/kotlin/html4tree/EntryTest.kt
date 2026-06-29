package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.io.File

class EntryTest {
    @Test
    fun testDataClasses() {
        val f = File("a")
        val e1 = Entry(f, 0, null)
        val e2 = Entry(f, 0, null)
        val e3 = Entry(File("b"), 1, null)
        assertEquals(e1, e2)
        assertEquals(e1.hashCode(), e2.hashCode())
        assertNotEquals(e1, e3)
        assertEquals("Entry(data=a, level=0, next=null)", e1.toString())

        val le1 = LinkedListEntry(f, 0)
        val le2 = LinkedListEntry(f, 0)
        val le3 = LinkedListEntry(File("b"), 1)
        assertEquals(le1, le2)
        assertEquals(le1.hashCode(), le2.hashCode())
        assertNotEquals(le1, le3)
        assertEquals("LinkedListEntry(file=a, level=0)", le1.toString())
    }
}
