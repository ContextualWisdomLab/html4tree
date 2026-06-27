package html4tree

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import java.io.File

class UtilTest {

    @Test
    fun testLinkedList() {
        val list = LinkedList()

        // pull on empty list
        assertNull(list.pull())

        val file1 = File("file1")
        val file2 = File("file2")
        val file3 = File("file3")

        // push one and pull
        list.push(LinkedListEntry(file1, 0))
        val entry1 = list.pull()
        assertEquals(file1, entry1?.file)
        assertEquals(0, entry1?.level)
        assertNull(list.pull())

        // push multiple
        list.push(LinkedListEntry(file1, 1))
        list.push(LinkedListEntry(file2, 2))
        list.push(LinkedListEntry(file3, 3))

        val e1 = list.pull()
        assertEquals(file1, e1?.file)
        assertEquals(1, e1?.level)

        val e2 = list.pull()
        assertEquals(file2, e2?.file)
        assertEquals(2, e2?.level)

        val e3 = list.pull()
        assertEquals(file3, e3?.file)
        assertEquals(3, e3?.level)

        assertNull(list.pull())
    }
}
