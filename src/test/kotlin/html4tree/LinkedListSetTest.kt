package html4tree

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull
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
        assertNull(list2.first)
        val pulled = list2.pull()
        assertNotNull(pulled)
        assertEquals(File("f1"), pulled.file)
        assertEquals(1, pulled.level)
        assertNull(list2.pull())
    }
}
