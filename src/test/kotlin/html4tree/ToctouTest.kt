package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertTrue
import kotlin.concurrent.thread
import java.nio.file.Files

class ToctouTest {
    @Test
    fun testProcessIgnoreFileToctouExceptionRace() {
        val tempDir = Files.createTempDirectory("toctoutest").toFile()
        val ignoreFile = File(tempDir, ".html4ignore")

        var excluded: Set<String>? = null
        for (i in 0..2000) {
            ignoreFile.writeText("test.txt")
            val t = thread { ignoreFile.delete() }
            excluded = process_ignore_file(tempDir, null)
            t.join()
        }
        assertTrue(excluded?.contains("index.html") ?: false)
    }

    @Test
    fun testProcessIgnoreFileToctouException() {
        val tempDir = Files.createTempDirectory("toctoutest2").toFile()
        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText("test.txt")

        // Force an IOException during useLines
        val method = java.io.File::class.java.getDeclaredMethod("setReadable", Boolean::class.java)
        method.isAccessible = true
        method.invoke(ignoreFile, false)
        val excluded = process_ignore_file(tempDir, null)
        assertTrue(excluded.contains("index.html"))
    }
}
