package html4tree

import org.junit.Test
import java.io.File

class LinkedListPullNullTest {
    @Test
    fun testGoNotDir() {
        val f = File.createTempFile("temp", "txt")
        f.deleteOnExit()
        val ll = LinkedList()
        ll.push(LinkedListEntry(f, 0))
        val lle = ll.pull()
        assert(lle != null && !lle.file.isDirectory())
    }
}
