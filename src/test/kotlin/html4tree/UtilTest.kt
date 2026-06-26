package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.io.File

class UtilTest {

    @Test
    fun testLinkedList() {
        val list = LinkedList()
        assertNull(list.pull())

        val f1 = File("f1")
        val f2 = File("f2")

        list.push(LinkedListEntry(f1, 0))
        list.push(LinkedListEntry(f2, 1))

        // Coverage for pushing when last is not null
        list.push(LinkedListEntry(File("f3"), 2))

        // Coverage for pushing when first is null but last is not (this branch is mathematically impossible to reach in normal usage, but we can try to force it or set first to null)
        list.first = null
        list.push(LinkedListEntry(File("f4"), 3))

        val e1 = list.pull()
        assertNotNull(e1)
        assertEquals(f1, e1?.file)
        assertEquals(0, e1?.level)

        val e2 = list.pull()
        assertNotNull(e2)
        assertEquals(f2, e2?.file)
        assertEquals(1, e2?.level)

        val e3 = list.pull()
        assertNotNull(e3)
        assertEquals(File("f3"), e3?.file)

        assertNull(list.pull())

        // Coverage for getters/setters
        list.first = null
        list.last = null
        assertNull(list.first)
        assertNull(list.last)
    }

    @Test
    fun testDataClasses() {
        val entry = Entry(File("a"), 1, null)
        assertEquals(File("a"), entry.data)
        assertEquals(1, entry.level)
        assertNull(entry.next)

        val llEntry = LinkedListEntry(File("b"), 2)
        assertEquals(File("b"), llEntry.file)
        assertEquals(2, llEntry.level)
    }
}
