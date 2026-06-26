package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LinkedListTest2 {
    @Test
    fun testPushNullFirst() {
        val list = LinkedList()
        list.first = null
        list.last = null
        val file1 = File("file1")
        list.push(LinkedListEntry(file1, 0))
        assertEquals(file1, list.pull()?.file)
    }

    @Test
    fun testPullMultipleEmpty() {
        val list = LinkedList()
        assertNull(list.pull())
        assertNull(list.pull())
    }
}
