package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class PosixFallbackTest {
    @Test
    fun testWriteIndexFileWithoutPosixSupport() {
        val tempDir = java.nio.file.Files.createTempDirectory("test-posix").toFile()
        try {
            write_index_file(
                curr_dir = tempDir,
                content = "<html></html>",
                supportsPosix = false
            )
            val indexFile = File(tempDir, "index.html")
            assertTrue(indexFile.exists(), "index.html should be created even without POSIX support")
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
