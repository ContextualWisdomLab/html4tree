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
    fun testLinkedListPreservesFileKey() {
        val key = Any()
        val list = LinkedList()

        list.push(LinkedListEntry(File("secure"), 1, key))

        val pulled = list.pull()
        assertEquals(File("secure"), pulled?.file)
        assertEquals(1, pulled?.level)
        assertEquals(key, pulled?.fileKey)
}

}
