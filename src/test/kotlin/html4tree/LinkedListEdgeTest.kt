package html4tree

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull
import java.io.File

class LinkedListEdgeTest {
    @Test
    fun testLinkedListEdge() {
        val list = LinkedList()
        // test push on null last
        list.push(LinkedListEntry(File("f1"), 1))

        // internal check
        assertNotNull(list.last)
        assertNotNull(list.first)

        // test push on non-null last
        list.push(LinkedListEntry(File("f2"), 2))

        // pull first item
        val pulled1 = list.pull()
        assertEquals("f1", pulled1?.file?.name)

        // pull second item
        val pulled2 = list.pull()
        assertEquals("f2", pulled2?.file?.name)

        // pull on empty list again
        assertNull(list.pull())
    }
}
