package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.io.File

class LinkedListPullNullTest {
    @Test
    fun testLinkedListPullNull() {
        val list = LinkedList()
        list.last = Entry(File("a"), 0, null)
        list.pull()

        list.last = Entry(File("b"), 0, Entry(File("c"), 0, null))
        list.pull()
    }
}
