package html4tree

import org.junit.Test
import kotlin.test.assertEquals
import java.io.File

class CoverageTest {
    @Test
    fun testDataClasses() {
        val entry = Entry(File("test"), 0, null)
        assertEquals(0, entry.level)
        assertEquals(File("test"), entry.data)
        assertEquals(null, entry.next)

        // data class
        entry.toString()
        entry.hashCode()
        entry.copy()
        val entry2 = Entry(File("test"), 0, null)
        entry.equals(entry2)

        val llEntry = LinkedListEntry(File("test2"), 1)
        assertEquals(1, llEntry.level)
        assertEquals(File("test2"), llEntry.file)

        // data class
        llEntry.toString()
        llEntry.hashCode()
        llEntry.copy()
        val llEntry2 = LinkedListEntry(File("test2"), 1)
        llEntry.equals(llEntry2)
    }


    @Test
    fun testDataClassesMore() {
        val entry1 = Entry(File("t1"), 1, null)
        val entry2 = Entry(File("t2"), 2, entry1)
        entry1.component1()
        entry1.component2()
        entry1.component3()

        val llEntry1 = LinkedListEntry(File("l1"), 1)
        llEntry1.component1()
        llEntry1.component2()
    }


    @Test
    fun testLinkedListEntryGetterSetter() {
        val ll = LinkedList()
        ll.first = Entry(File("tmp1"), 1, null)
        val f = ll.first
        assertEquals(1, f?.level)
        val l = Entry(File("tmp2"), 2, null)
        ll.last = l
        assertEquals(2, ll.last?.level)
    }

}
