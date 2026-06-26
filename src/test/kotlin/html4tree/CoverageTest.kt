package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class CoverageTest {

    @Test
    fun testDataClasses() {
        val entry = Entry(File("test"), 0, null)
        assertEquals("Entry(data=test, level=0, next=null)", entry.toString())
        assertEquals(entry, entry.copy())
        assertEquals(entry.hashCode(), entry.copy().hashCode())

        val lle = LinkedListEntry(File("test2"), 1)
        assertEquals("LinkedListEntry(file=test2, level=1)", lle.toString())
        assertEquals(lle, lle.copy())
        assertEquals(lle.hashCode(), lle.copy().hashCode())
    }

    @Test
    fun testLinkedList() {
        val list = LinkedList()
        list.push(LinkedListEntry(File("a"), 0))
        list.push(LinkedListEntry(File("b"), 1))
        list.push(LinkedListEntry(File("c"), 2))

        assertEquals("a", list.pull()?.file?.name)
        assertEquals("b", list.pull()?.file?.name)
        assertEquals("c", list.pull()?.file?.name)
    }
}
