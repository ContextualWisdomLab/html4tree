package html4tree

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import java.io.File

class LinkedListAdvancedTest {
    @Test
    fun testLinkedListFull() {
        val list = LinkedList()
        list.push(LinkedListEntry(File("f1"), 1))

        // This is to hit specific branch cases. LinkedList logic is a bit weird.
        // It acts somewhat like a queue but the pointers are called first and last.
        // last = first node added
        // first = last node added

        list.push(LinkedListEntry(File("f2"), 2))
        list.push(LinkedListEntry(File("f3"), 3))

        val p1 = list.pull()
        val p2 = list.pull()
        val p3 = list.pull()

        assertEquals("f1", p1?.file?.name)
        assertEquals("f2", p2?.file?.name)
        assertEquals("f3", p3?.file?.name)
    }
}
