package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.nio.file.Files

class ToctouTest {
    @Test
    fun testProcessIgnoreFileToctouIoException() {
        val tempDir = Files.createTempDirectory("test-toctou").toFile()
        try {
            val ignoreFile = File(tempDir, ".html4ignore")
            val done = java.util.concurrent.atomic.AtomicBoolean(false)
            val t = kotlin.concurrent.thread {
                while (!done.get()) {
                    ignoreFile.writeText("*.txt")
                    ignoreFile.setReadable(true)
                    ignoreFile.setReadable(false)
                    ignoreFile.delete()
                }
            }

            for (i in 1..5000) {
                process_ignore_file(tempDir, null)
            }
            done.set(true)
            t.join()
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
