package html4tree

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import java.io.File

class UtilTest {
    @Test
    fun testLinkedList() {
        val list = LinkedList()

        assertNull(list.pull())

        val file1 = File("file1")
        val file2 = File("file2")
        val file3 = File("file3")

        list.push(LinkedListEntry(file1, 0))
        list.push(LinkedListEntry(file2, 1))
        list.push(LinkedListEntry(file3, 2))

        var pulled = list.pull()
        assertEquals(file1, pulled?.file)
        assertEquals(0, pulled?.level)

        pulled = list.pull()
        assertEquals(file2, pulled?.file)
        assertEquals(1, pulled?.level)

        pulled = list.pull()
        assertEquals(file3, pulled?.file)
        assertEquals(2, pulled?.level)
        assertNull(list.pull())
    }
}
