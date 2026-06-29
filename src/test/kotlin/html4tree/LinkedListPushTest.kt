package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.io.File

class LinkedListPushTest {
    @Test
    fun testLinkedListPushNullNext() {
        val list = LinkedList()
        val e1 = LinkedListEntry(File("a"), 0)

        list.last = Entry(File("init"), 0, null)
        list.first = null

        list.push(e1)

        assertNull(list.first)
    }
}
