package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LinkedListTest {

    @Test
    fun testLinkedListEntry() {
        val file = File("test")
        val entry = LinkedListEntry(file, 1)
        assertEquals(file, entry.file)
        assertEquals(1, entry.level)
        val entryCopy = entry.copy()
        assertEquals(entry, entryCopy)
        assertTrue(entry.hashCode() == entryCopy.hashCode())
        assertTrue(entry.toString().contains("LinkedListEntry"))
        assertEquals(file, entry.component1())
        assertEquals(1, entry.component2())
    }

    @Test
    fun testEntry() {
        val file = File("test")
        var entry = Entry(file, 1, null)
        assertEquals(file, entry.data)
        assertEquals(1, entry.level)
        assertNull(entry.next)
        val entry2 = Entry(file, 2, null)
        entry.next = entry2
        assertEquals(entry2, entry.next)
        val entryCopy = entry.copy()
        assertEquals(entry, entryCopy)
        assertTrue(entry.hashCode() == entryCopy.hashCode())
        assertTrue(entry.toString().contains("Entry"))
        assertEquals(file, entry.component1())
        assertEquals(1, entry.component2())
        assertEquals(entry2, entry.component3())
    }

    @Test
    fun testLinkedListPushPull() {
        val list = LinkedList()

        assertNull(list.first)
        assertNull(list.last)

        assertNull(list.pull())

        val entry1 = LinkedListEntry(File("test1"), 1)
        val entry2 = LinkedListEntry(File("test2"), 2)

        list.push(entry1)

        assertNotNull(list.first)
        assertNotNull(list.last)
        assertEquals(File("test1"), list.first?.data)
        assertEquals(File("test1"), list.last?.data)

        list.push(entry2)

        val pulled1 = list.pull()
        assertNotNull(pulled1)
        assertEquals(File("test1"), pulled1?.file)
        assertEquals(1, pulled1?.level)

        val pulled2 = list.pull()
        assertNotNull(pulled2)
        assertEquals(File("test2"), pulled2?.file)
        assertEquals(2, pulled2?.level)

        assertNull(list.pull())

        // Add additional push to make sure branch coverage for first?.next is hit properly
        val list2 = LinkedList()
        list2.push(LinkedListEntry(File("testA"), 1))
        list2.push(LinkedListEntry(File("testB"), 2))
        list2.push(LinkedListEntry(File("testC"), 3))

        assertEquals(File("testA"), list2.pull()?.file)
        assertEquals(File("testB"), list2.pull()?.file)
        assertEquals(File("testC"), list2.pull()?.file)
        assertNull(list2.pull())

        // This hits the case where last != null but first == null (edge case handling of the push method structure)
        val list3 = LinkedList()
        list3.last = Entry(File("something"), 0, null)
        list3.push(LinkedListEntry(File("something else"), 1))

        list.first = Entry(File("test3"), 3, null)
        list.last = Entry(File("test4"), 4, null)
        assertNotNull(list.first)
        assertNotNull(list.last)

        list.first = null
        list.last = null
        assertNull(list.first)
        assertNull(list.last)
    }
}
