package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files

class BranchTest9 {
    @Test
    fun testPushWithExistingLast() {
        // hits "else" branch in push
        val ll = LinkedList()
        ll.push(LinkedListEntry(File("1"), 1))
        ll.push(LinkedListEntry(File("2"), 2))
        assert(ll.first?.next == null)
    }
}
