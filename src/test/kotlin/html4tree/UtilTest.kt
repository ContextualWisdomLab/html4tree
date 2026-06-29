package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull

class UtilTest {
    @Test
    fun testLinkedList() {
        val list = LinkedList()
        val file1 = File("dir1")
        val file2 = File("dir2")
        val file3 = File("dir3")

        val entry1 = LinkedListEntry(file1, 0)
        val entry2 = LinkedListEntry(file2, 1)
        val entry3 = LinkedListEntry(file3, 2)

        // setter, getter 커버리지
        val mockEntry1 = Entry(file1, 0, null)
        val mockEntry2 = Entry(file2, 1, null)

        list.first = mockEntry1
        assertEquals(mockEntry1, list.first)
        list.last = mockEntry2
        assertEquals(mockEntry2, list.last)

        list.first = null
        list.last = null

        // getFirst(), getLast() 커버리지
        list.first
        list.last

        // 빈 리스트에서 pull 테스트
        assertNull(list.pull())

        // 1개 push 후 pull
        list.push(entry1)
        list.first
        list.last

        // 2개 push하여 분기문 추가 커버
        list.push(entry2)

        var result = list.pull()
        assertNotNull(result)
        assertEquals(file1, result?.file)
        assertEquals(0, result?.level)

        result = list.pull()
        assertNotNull(result)
        assertEquals(file2, result?.file)
        assertEquals(1, result?.level)

        assertNull(list.pull())

        // 여러 개 push 후 순서대로 pull
        list.push(entry1)
        list.push(entry2)
        list.push(entry3)

        result = list.pull()
        assertEquals(file1, result?.file)

        result = list.pull()
        assertEquals(file2, result?.file)

        result = list.pull()
        assertEquals(file3, result?.file)

        assertNull(list.pull())
    }
}