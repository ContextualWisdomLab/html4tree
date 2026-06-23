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

        val f1 = File("file1")
        val f2 = File("file2")

        list.push(LinkedListEntry(f1, 0))
        list.push(LinkedListEntry(f2, 1))

        val entry1 = list.pull()
        assertEquals(f1, entry1?.file)
        assertEquals(0, entry1?.level)

        val entry2 = list.pull()
        assertEquals(f2, entry2?.file)
        assertEquals(1, entry2?.level)

        assertNull(list.pull())
    }
}
