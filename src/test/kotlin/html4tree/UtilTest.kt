package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

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

    @Test
    fun testEntryDataClass() {
        val file1 = File("file1")
        val entry1 = Entry(file1, 0, null)
        val entry2 = Entry(file1, 0, null)

        assertEquals(entry1, entry2)
        assertEquals("Entry(data=file1, level=0, next=null, fileKey=null)", entry1.toString())
    }

    @Test
    fun testEntryDataClassGeneratedMembers() {
        val file1 = File("file1")
        val entry = Entry(file1, 0, null)

        assertEquals(entry, entry)
        assertEquals(Entry(file1, 0, null).hashCode(), entry.hashCode())
        assertNotEquals<Any>(entry, "not an entry")
        assertNotEquals(entry, Entry(File("file2"), 0, null))
        assertNotEquals(entry, Entry(file1, 1, null))
        assertNotEquals(entry, Entry(file1, 0, Entry(file1, 1, null)))

        val copied = entry.copy(level = 2)
        assertEquals(file1, copied.data)
        assertEquals(2, copied.level)
        assertNull(copied.next)

        val (data, level, next) = entry
        assertEquals(file1, data)
        assertEquals(0, level)
        assertNull(next)
    }

    @Test
    fun testLinkedListEntryDataClass() {
        val file1 = File("file1")
        val entry1 = LinkedListEntry(file1, 0)
        val entry2 = LinkedListEntry(file1, 0)

        assertEquals(entry1, entry2)
        assertEquals("LinkedListEntry(file=file1, level=0, fileKey=null)", entry1.toString())
    }

    @Test
    fun testLinkedListEntryDataClassGeneratedMembers() {
        val file1 = File("file1")
        val entry = LinkedListEntry(file1, 0)

        assertEquals(entry, entry)
        assertEquals(LinkedListEntry(file1, 0).hashCode(), entry.hashCode())
        assertNotEquals<Any>(entry, "not an entry")
        assertNotEquals(entry, LinkedListEntry(File("file2"), 0))
        assertNotEquals(entry, LinkedListEntry(file1, 1))

        val copied = entry.copy(level = 5)
        assertEquals(file1, copied.file)
        assertEquals(5, copied.level)

        val (file, level, fileKey) = entry
        assertEquals(file1, file)
        assertEquals(0, level)
        assertNull(fileKey)
    }

    @Test
    fun testLinkedListPushExisting() {
        val list = LinkedList()
        list.push(LinkedListEntry(File("f1"), 0))
        list.push(LinkedListEntry(File("f2"), 0))
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
        assertEquals(File("fake"), list.pull()?.file)
        assertEquals(File("f3"), list.pull()?.file)
        assertEquals(File("f3"), list.first?.data)
    }

    @Test
    fun testLinkedListPreservesFileKey() {
        val key = Any()
        val list = LinkedList()

        list.push(LinkedListEntry(File("secure"), 1, key))

        val pulled = list.pull()
        assertEquals(File("secure"), pulled?.file)
        assertEquals(1, pulled?.level)
        assertEquals(key, pulled?.fileKey)
    }

    @Test
    fun testLinkedListPushNullFirstWithExistingChain() {
        val list = LinkedList()
        list.last = Entry(File("f1"), 0, Entry(File("f2"), 0, null))
        list.push(LinkedListEntry(File("f3"), 0))

        assertEquals(File("f1"), list.pull()?.file)
        assertEquals(File("f2"), list.pull()?.file)
        assertEquals(File("f3"), list.pull()?.file)
        assertNull(list.pull())
    }
}
