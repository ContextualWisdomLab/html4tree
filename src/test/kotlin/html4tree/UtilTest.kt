package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class UtilTest {

    @Test
    fun testLinkedListEntryDataClass() {
        val file1 = File("file1")
        val entry1 = LinkedListEntry(file1, 0)
        val entry2 = LinkedListEntry(file1, 0)

        assertEquals(entry1, entry2)
        assertEquals("LinkedListEntry(file=file1, level=0)", entry1.toString())
    }

}
