package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertFalse

class AttrExceptionTest {
    @Test
    fun testUnreadableEntryRemovesTheWholeGeneratedIndex() {
        val tempDir = Files.createTempDirectory("attr_test").toFile()
        try {
            val visibleFile = File(tempDir, "visible.txt").apply { writeText("visible") }
            val nonExistentFile = File(tempDir, "does_not_exist")
            val existingIndex = File(tempDir, "index.html").apply {
                writeText("stale listing that must not survive")
            }

            process_dir(tempDir, setOf(), arrayOf(visibleFile, nonExistentFile))

            assertFalse(
                existingIndex.exists(),
                "A metadata failure must remove the complete generated index instead of publishing a partial list"
            )
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
