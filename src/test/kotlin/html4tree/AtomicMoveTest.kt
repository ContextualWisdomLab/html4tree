package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertTrue
import java.nio.file.Files
import java.nio.file.AtomicMoveNotSupportedException

class AtomicMoveTest {
    @Test
    fun testAtomicMoveNotSupported() {
        val tempDir = Files.createTempDirectory("atomic_test").toFile()
        try {
            write_index_file(
                tempDir,
                "test content",
                moveFile = { _, _ -> throw AtomicMoveNotSupportedException("source", "target", "Not supported") }
            )
            val indexHtml = File(tempDir, "index.html")
            assertTrue(indexHtml.exists())
            assertTrue(indexHtml.readText() == "test content")
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
