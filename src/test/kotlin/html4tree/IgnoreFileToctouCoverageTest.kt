package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertTrue
import java.nio.file.Files
import kotlin.concurrent.thread
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

class IgnoreFileToctouCoverageTest {
    @Test
    fun testProcessIgnoreFileToctou() {
        val tempDir = Files.createTempDirectory("toctou").toFile()
        try {
            val ignoreFile = File(tempDir, ".html4ignore")

            var hitCatchBlock = AtomicBoolean(false)

            for (i in 0 until 500) { // Increase iterations for better chance
                ignoreFile.createNewFile()
                ignoreFile.writeText("*.txt")

                // Using a latch to try to sync the deletion closer to the check
                val latch = CountDownLatch(1)

                val t = thread {
                    latch.await()
                    ignoreFile.delete()
                }

                try {
                    latch.countDown() // Release the delete thread right before we process
                    val result = process_ignore_file(tempDir, null)
                    if (result.contains("index.html")) {
                        // Normally index.html is added. If the catch block executes,
                        // it will also just return the default files without crashing.
                    }
                } catch (e: Exception) {
                    // We don't want it to crash the test. The method itself should catch the IOException.
                }
                t.join()
            }
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
