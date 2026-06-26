package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LinkedListTest3 {
    @Test
    fun testGetters() {
        val list = LinkedList()
        assertNull(list.first)
        assertNull(list.last)

        val entry = Entry(File("f"), 0, null)
        list.first = entry
        list.last = entry

        assertEquals(entry, list.first)
        assertEquals(entry, list.last)
    }
}
