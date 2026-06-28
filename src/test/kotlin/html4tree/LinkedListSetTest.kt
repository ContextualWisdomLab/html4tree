package html4tree

import org.junit.Test
import kotlin.test.assertEquals
import java.io.File

class LinkedListSetTest {
    @Test
    fun testLinkedListSettersAndPush() {
        val list = LinkedList()

        // Use the generated setters
        val e = Entry(File("test"), 0, null)
        list.first = e
        list.last = e

        assertEquals(e, list.first)
        assertEquals(e, list.last)

        val list2 = LinkedList()
        list2.push(LinkedListEntry(File("f1"), 1))
        list2.first = null
        list2.push(LinkedListEntry(File("f2"), 2))

        val pulled = list2.pull()
        assertEquals("f1", pulled?.file?.name)
        assertEquals(1, pulled?.level)
        assertEquals(null, list2.pull())
    }
}
