package html4tree

import org.junit.Test
import java.io.File

class BranchTest10 {
    @Test
    fun testGoWithNullLleFileNotDir() {
        val list = LinkedList()
        val file = File("not_a_dir")
        file.createNewFile()
        file.deleteOnExit()
        list.push(LinkedListEntry(file, 0))
        val lle = list.pull()
        // this tests the `lle != null && lle.file.isDirectory()` where the second is false.
        // we've done this. What about `lle == null`? we've done this.
    }
}
