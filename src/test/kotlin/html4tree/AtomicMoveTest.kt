package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertTrue
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Path
import java.nio.file.CopyOption

class AtomicMoveTest {
    @Test
    fun testWriteIndexFileAtomicMoveFallback() {
        val tempDir = Files.createTempDirectory("atomic_test").toFile()
        try {
            var moveCalled = 0
            val moveMock: (Path, Path, Array<out CopyOption>) -> Path = { src, dest, options ->
                moveCalled++
                if (options.contains(StandardCopyOption.ATOMIC_MOVE)) {
                    throw AtomicMoveNotSupportedException(src.toString(), dest.toString(), "Mocked exception")
                } else {
                    Files.move(src, dest, *options)
                }
            }

            write_index_file(tempDir, "test content", moveMock)

            val indexHtml = File(tempDir, "index.html")
            assertTrue(indexHtml.exists())
            assertTrue(moveCalled >= 2, "Fallback should be called")
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
