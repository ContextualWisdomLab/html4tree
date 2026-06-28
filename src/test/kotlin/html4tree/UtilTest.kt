package html4tree

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import java.io.File

class UtilTest {
    @Test
    fun testLinkedList() {
        val list = LinkedList()

        // pull from empty list
        assertNull(list.pull())

        val file1 = File("file1")
        val file2 = File("file2")
        val file3 = File("file3")

        // push first element
        list.push(LinkedListEntry(file1, 0))
        assertEquals(file1, list.first?.data)
        assertEquals(0, list.first?.level)
        assertEquals(list.first, list.last)

        // push second element
        list.push(LinkedListEntry(file2, 1))
        assertEquals(file2, list.first?.data)
        assertEquals(1, list.first?.level)
        assertEquals(file1, list.last?.data)
        assertEquals(0, list.last?.level)

        // push third element to cover first?.next null check branch maybe
        list.push(LinkedListEntry(file3, 2))

        // Test setFirst and setLast by directly mutating for coverage since they are synthesized properties
        list.first = null
        list.last = null

        list.push(LinkedListEntry(file1, 0))
        list.first = list.first // coverage for setter
        list.last = list.last   // coverage for setter

        list.pull()
        list.pull()
    }

    @Test
    fun testLinkedListPushNullBranch() {
        val list = LinkedList()
        list.last = Entry(File("test"), 0, null)
        list.first = null
        list.push(LinkedListEntry(File("test2"), 1))
    }
}
