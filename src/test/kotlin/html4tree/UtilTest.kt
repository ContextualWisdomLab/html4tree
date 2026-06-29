package html4tree

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UtilTest {

    @Test
    fun testLinkedListEntry() {
        val file = java.io.File("some-file")
        val entry = LinkedListEntry(file, 2)
        assertEquals(file, entry.file)
        assertEquals(2, entry.level)
    }

    @Test
    fun testEntry() {
        val file = java.io.File("some-file")
        val entry = Entry(file, 1, null)
        assertEquals(file, entry.data)
        assertEquals(1, entry.level)
        assertNull(entry.next)
    }

    @Test
    fun testLinkedListPushPull() {
        val list = LinkedList()
        val file1 = java.io.File("file1")
        val file2 = java.io.File("file2")

        assertNull(list.pull())

        list.push(LinkedListEntry(file1, 0))
        list.push(LinkedListEntry(file2, 1))

        val entry1 = list.pull()
        val entry2 = list.pull()
        val entry3 = list.pull()

        assertEquals(file1, entry1?.file)
        assertEquals(0, entry1?.level)

        assertEquals(file2, entry2?.file)
        assertEquals(1, entry2?.level)

        assertNull(entry3)

        // extra check for empty state handling
        val list2 = LinkedList()
        list2.push(LinkedListEntry(file1, 0))
        list2.pull()
        assertNull(list2.pull())
    }

    @Test
    fun testLinkedListGettersSetters() {
        val list = LinkedList()
        val file1 = java.io.File("file1")
        val entry = Entry(file1, 0, null)

        list.first = entry
        list.last = entry

        assertEquals(entry, list.first)
        assertEquals(entry, list.last)
    }
}
