package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LinkedListTest {
    @Test
    fun testPushAndPull() {
        val list = LinkedList()
        val file1 = File("file1")
        val file2 = File("file2")

        list.push(LinkedListEntry(file1, 0))
        list.push(LinkedListEntry(file2, 1))

        val entry1 = list.pull()
        assertEquals(file1, entry1?.file)
        assertEquals(0, entry1?.level)

        val entry2 = list.pull()
        assertEquals(file2, entry2?.file)
        assertEquals(1, entry2?.level)

        assertNull(list.pull())
    }

    @Test
    fun testPullEmpty() {
        val list = LinkedList()
        assertNull(list.pull())
    }
}
