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

        // Push logic where first is somehow null but last isn't (impossible in normal flow, but let's see if we can trick the compiler or hit the safe call ?. )
        // Actually, let's just make sure we hit the "first?.next =" and "first = first?.next"
        val list2 = LinkedList()
        list2.push(LinkedListEntry(File("f1"), 1))

        // list2.last is not null, list2.first is not null.
        list2.first = null
        list2.push(LinkedListEntry(File("f2"), 2))

        // By doing this, first is null when push is called, so first?.next does nothing safely
    }
}
