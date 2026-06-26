package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class LinkedListTest5 {
    @Test
    fun testPushNullSafe() {
        val list = LinkedList()
        val file1 = File("file1")
        val file2 = File("file2")

        list.push(LinkedListEntry(file1, 0))
        list.first = null
        list.push(LinkedListEntry(file2, 1))

        assertEquals(file1, list.pull()?.file)
    }
}
