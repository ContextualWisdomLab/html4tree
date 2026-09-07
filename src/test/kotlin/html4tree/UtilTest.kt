package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class UtilTest {
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
}
