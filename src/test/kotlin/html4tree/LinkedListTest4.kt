package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class LinkedListTest4 {
    @Test
    fun testPushBranch() {
        val list = LinkedList()
        val file1 = File("file1")
        val file2 = File("file2")
        val file3 = File("file3")

        list.push(LinkedListEntry(file1, 0))
        list.push(LinkedListEntry(file2, 1))
        list.push(LinkedListEntry(file3, 2))

        val entry1 = list.pull()
        val entry2 = list.pull()
        val entry3 = list.pull()

        assertEquals(file1, entry1?.file)
        assertEquals(file2, entry2?.file)
        assertEquals(file3, entry3?.file)
    }
}
