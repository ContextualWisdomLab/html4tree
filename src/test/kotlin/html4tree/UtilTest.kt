package html4tree

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import java.io.File

class UtilTest {

    @Test
    fun testLinkedList() {
        val ll = LinkedList()

        // 초기에 pull() 하면 null이 나와야 함
        assertNull(ll.pull())

        val file1 = File("test1")
        val file2 = File("test2")

        val entry1 = LinkedListEntry(file1, 0)
        val entry2 = LinkedListEntry(file2, 1)

        ll.push(entry1)
        ll.push(entry2)

        // Queue처럼 처음 들어간 것부터 나와야 함
        val out1 = ll.pull()
        assertEquals(file1, out1?.file)
        assertEquals(0, out1?.level)

        val out2 = ll.pull()
        assertEquals(file2, out2?.file)
        assertEquals(1, out2?.level)

        assertNull(ll.pull())
    }

    @Test
    fun testEscapeHtml() {
        assertEquals("&amp;", "&".escapeHtml())
        assertEquals("&lt;", "<".escapeHtml())
        assertEquals("&gt;", ">".escapeHtml())
        assertEquals("&quot;", "\"".escapeHtml())
        assertEquals("&#x27;", "'".escapeHtml())
        assertEquals("&lt;div class=&quot;test&quot;&gt;Hello &amp; Welcome&#x27;&lt;/div&gt;", "<div class=\"test\">Hello & Welcome'</div>".escapeHtml())
    }

    @Test
    fun testUrlEncodePath() {
        assertEquals("hello%20world", "hello world".urlEncodePath())
        assertEquals("test%2B%3F%26", "test+?&".urlEncodePath())
    }


    @Test
    fun testLinkedListFull() {
        val ll = LinkedList()
        val file1 = File("f1")
        val entry1 = LinkedListEntry(file1, 0)
        ll.push(entry1)
        val file2 = File("f2")
        val entry2 = LinkedListEntry(file2, 1)
        ll.push(entry2)
        val file3 = File("f3")
        val entry3 = LinkedListEntry(file3, 2)
        ll.push(entry3)

        assertEquals(file1, ll.pull()?.file)
        assertEquals(file2, ll.pull()?.file)
        assertEquals(file3, ll.pull()?.file)
        assertNull(ll.pull())
    }


    @Test
    fun testLinkedListPushFirstNotNull() {
        val ll = LinkedList()
        val entry1 = LinkedListEntry(File("f1"), 0)
        ll.push(entry1)
        val entry2 = LinkedListEntry(File("f2"), 0)
        ll.push(entry2)
        assertEquals(File("f1"), ll.pull()?.file)
        assertEquals(File("f2"), ll.pull()?.file)
    }

    @Test
    fun testLinkedListPullNextNotNull() {
        val ll = LinkedList()
        val entry1 = LinkedListEntry(File("f1"), 0)
        ll.push(entry1)
        val entry2 = LinkedListEntry(File("f2"), 0)
        ll.push(entry2)
        ll.pull()
        ll.pull()
        assertNull(ll.pull())
    }



    @Test
    fun testLinkedListPushNullFirst() {
        val ll = LinkedList()
        ll.first = null
        ll.last = Entry(File("tmp"), 0, null)
        val entry = LinkedListEntry(File("tmp2"), 0)
        ll.push(entry)
        assertNull(ll.first?.next)
    }

    @Test
    fun testLinkedListPullNullFirst() {
        val ll = LinkedList()
        ll.first = null
        ll.last = Entry(File("tmp"), 0, null)
        val pulled = ll.pull()
        assertEquals(File("tmp"), pulled?.file)
    }



    @Test
    fun testLinkedListEntryConstructors() {
        val ll = LinkedList()
        val e = Entry(File("a"), 0, null)
        ll.first = e
        ll.last = e
        assertEquals(e, ll.first)
    }

}
