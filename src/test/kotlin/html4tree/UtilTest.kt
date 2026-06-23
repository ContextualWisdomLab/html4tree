package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class UtilTest {

    @Test
    fun testLinkedListPushAndPull() {
        val list = LinkedList()
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

        val entry3 = list.pull()
        assertEquals(null, entry3)
    }
}
