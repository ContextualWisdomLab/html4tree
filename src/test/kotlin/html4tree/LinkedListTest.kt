package html4tree

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull
import java.io.File

class LinkedListTest {
    @Test
    fun testPushAndPull() {
        val list = LinkedList()
        assertNull(list.pull())

        val file1 = File("file1")
        val entry1 = LinkedListEntry(file1, 0)
        list.push(entry1)

        val file2 = File("file2")
        val entry2 = LinkedListEntry(file2, 1)
        list.push(entry2)

        val pulled1 = list.pull()
        assertEquals(file1, pulled1?.file)
        assertEquals(0, pulled1?.level)

        val pulled2 = list.pull()
        assertEquals(file2, pulled2?.file)
        assertEquals(1, pulled2?.level)

        assertNull(list.pull())
    }

    @Test
    fun testPushMultiple() {
        val list = LinkedList()
        val file1 = File("file1")
        val file2 = File("file2")
        val file3 = File("file3")

        list.push(LinkedListEntry(file1, 0))
        list.push(LinkedListEntry(file2, 1))
        list.push(LinkedListEntry(file3, 2))

        val pulled1 = list.pull()
        assertEquals(file1, pulled1?.file)

        val pulled2 = list.pull()
        assertEquals(file2, pulled2?.file)

        val pulled3 = list.pull()
        assertEquals(file3, pulled3?.file)

        assertNull(list.pull())
    }

    @Test
    fun testGettersAndSetters() {
        val list = LinkedList()

        val file1 = File("file1")
        val entry1 = Entry(file1, 0, null)

        list.first = entry1
        assertEquals(entry1, list.first)

        val file2 = File("file2")
        val entry2 = Entry(file2, 1, null)

        list.last = entry2
        assertEquals(entry2, list.last)
    }

    @Test
    fun testPushWithFirstNextBranch() {
        val list = LinkedList()

        val file1 = File("file1")
        list.push(LinkedListEntry(file1, 0))

        val file2 = File("file2")
        list.push(LinkedListEntry(file2, 1))

        val pulled1 = list.pull()
        assertNotNull(pulled1)
        assertEquals(file1, pulled1.file)
        assertEquals(0, pulled1.level)

        val pulled2 = list.pull()
        assertNotNull(pulled2)
        assertEquals(file2, pulled2.file)
        assertEquals(1, pulled2.level)

        assertNull(list.pull())
    }

    @Test
    fun testPushWhenFirstIsNullAndLastIsSet() {
        val list = LinkedList()

        val seed = Entry(File("seed"), 0, null)
        list.last = seed
        list.first = null

        val file2 = File("file2")
        list.push(LinkedListEntry(file2, 1))

        val pulled1 = list.pull()
        assertNotNull(pulled1)
        assertEquals(File("seed"), pulled1.file)

        assertNull(list.pull())
    }
}
