package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UtilTest {

    @Test
    fun testLinkedList() {
        val list = LinkedList()
        assertNull(list.pull())

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
    fun testEntryDataClass() {
        val file1 = File("file1")
        val entry1 = Entry(file1, 0, null)
        val entry2 = Entry(file1, 0, null)

        assertEquals(entry1, entry2)
        assertEquals("Entry(data=file1, level=0, next=null)", entry1.toString())
    }

    @Test
    fun testLinkedListEntryDataClass() {
        val file1 = File("file1")
        val entry1 = LinkedListEntry(file1, 0)
        val entry2 = LinkedListEntry(file1, 0)

        assertEquals(entry1, entry2)
        assertEquals("LinkedListEntry(file=file1, level=0)", entry1.toString())
    }

    @Test
    fun testLinkedListPushExisting() {
        val list = LinkedList()
        list.push(LinkedListEntry(File("f1"), 0))
        list.push(LinkedListEntry(File("f2"), 0)) // triggers the 'else' branch in push
        val entry1 = list.pull()
        val entry2 = list.pull()
        assertEquals(File("f1"), entry1?.file)
        assertEquals(File("f2"), entry2?.file)
    }

    @Test
    fun testLinkedListAccessors() {
        val list = LinkedList()
        list.first = Entry(File("test"), 0, null)
        list.last = Entry(File("test"), 0, null)
        assertEquals(File("test"), list.first?.data)
        assertEquals(File("test"), list.last?.data)
    }
    @Test
    fun testLinkedListPushNullFirst() {
        val list = LinkedList()
        list.last = Entry(File("fake"), 0, null)
        list.push(LinkedListEntry(File("f3"), 0))
        assertEquals(File("f3"), list.first?.data)
    }
}
