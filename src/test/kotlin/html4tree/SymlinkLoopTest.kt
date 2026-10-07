package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class SymlinkLoopTest {
    @Test
    fun testSymlinkLoopPrevention() {
        val rootDir = File("build/test-loop-dir").apply { mkdirs() }

        // This is a unit test directly for the logic. We'll set up a fake loop.
        val ll = LinkedList()
        val topEntry = LinkedListEntry(rootDir, 0, read_file_identity(rootDir).key)
        ll.push(topEntry)

        // Push the same root directory again as a child to simulate a loop that could occur
        // in some filesystems or via bind mounts.
        val loopEntry = LinkedListEntry(rootDir, 1, read_file_identity(rootDir).key)
        ll.push(loopEntry)

        var processedCount = 0
        crawl_directories(
            ll,
            -1,
            processDirectory = { _, _, _ -> processedCount++ },
            processIgnoreFile = { _, _ -> emptySet() },
            listFiles = { emptyArray() }
        )

        assertTrue(processedCount == 1, "Should process the root directory exactly once, preventing the loop")
    }
}
